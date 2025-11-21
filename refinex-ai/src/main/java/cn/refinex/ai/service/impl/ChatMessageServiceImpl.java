package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.chat.dto.request.ChatAttachmentPayload;
import cn.refinex.ai.controller.chat.dto.request.ChatMessagePageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatMessageSendRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.*;
import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.entity.*;
import cn.refinex.ai.enums.ChatMessageType;
import cn.refinex.ai.enums.ContentFormat;
import cn.refinex.ai.enums.MessageRole;
import cn.refinex.ai.enums.UsageOperation;
import cn.refinex.ai.model.ChatMessageSegment;
import cn.refinex.ai.repository.*;
import cn.refinex.ai.service.AiAgentService;
import cn.refinex.ai.service.ChatMessageService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.json.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * 聊天消息服务实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    /**
     * 上下文消息限制
     */
    private static final int CONTEXT_MESSAGE_LIMIT = 30;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final AiPromptRepository aiPromptRepository;
    private final AiAgentService aiAgentService;
    private final JsonUtils jsonUtils;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 消息元数据类型引用
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    /**
     * 消息段列表类型引用
     */
    private static final TypeReference<List<ChatMessageSegment>> SEGMENT_LIST_TYPE = new TypeReference<>() {
    };

    /**
     * 分页查询消息
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    @Override
    public PageResponse<ChatMessageResponseDTO> page(ChatMessagePageRequest request, Long userId) {
        ChatMessagePageRequest query = Objects.isNull(request) ? new ChatMessagePageRequest() : request;
        PageResponse<ChatMessage> page = chatMessageRepository.pageBySession(query.getSessionId(), userId, query);
        List<Long> messageIds = page.records().stream().map(ChatMessage::getId).toList();
        Map<Long, List<ChatAttachment>> attachmentMap = chatAttachmentRepository.groupByMessageIds(messageIds);
        return page.map(msg -> toResponse(msg, attachmentMap.getOrDefault(msg.getId(), List.of())));
    }

    /**
     * 发送消息 (同步阻塞)
     *
     * @param request 发送请求
     * @param userId  用户ID
     * @return 发送响应
     */
    @Override
    public ChatMessageSendResponseDTO send(ChatMessageSendRequestDTO request, Long userId) {
        // 准备会话，如果没有消息则视为新会话
        ChatSession session = prepareSession(request, userId);
        boolean newSession = session.getMessageCount() == null || session.getMessageCount() == 0;

        // 构建用户消息
        ChatMessage userMessage = buildUserMessage(request, session, userId);
        // 构建用户附件
        List<ChatAttachment> userAttachments = buildAttachments(request.attachments(), userMessage, userId);

        // 保存用户消息和附件
        jdbcManager.executeInTransaction(jdbc -> {
            // 保存用户消息
            Long messageId = chatMessageRepository.insert(userMessage, jdbc);
            userMessage.setId(messageId);

            // 保存用户附件
            if (!userAttachments.isEmpty()) {
                userAttachments.forEach(att -> att.setMessageId(messageId));
                chatAttachmentRepository.batchInsert(userAttachments, jdbc);
            }

            // 更新会话统计
            chatSessionRepository.incrementStats(session.getId(), userId, 1, null,
                    userMessage.getMessageTime(), summarizeSessionTitle(session, userMessage.getContentText()), jdbc);

            return null;
        });

        // 查询会话历史消息(30条)
        List<ChatMessage> history = chatMessageRepository.listRecent(session.getId(), CONTEXT_MESSAGE_LIMIT);
        // 构建运行时上下文
        ChatModelContext modelContext = aiAgentService.buildRuntimeContext(session.getAgentId(), userId);
        // 获取模型描述符
        ChatModelDescriptor descriptor = modelContext.getDescriptor();

        // 构建助手消息
        ChatMessage assistantMessage = baseAssistantMessage(session, userMessage, descriptor, userId);

        List<ChatMessageSegment> assistantSegments;
        Exception inferenceException = null;
        ChatResponseMetadata metadata = null;
        long startAt = System.nanoTime();

        try {
            // 构建提示消息
            Prompt prompt = new Prompt(buildPromptMessages(descriptor.getAgent(), history), modelContext.getChatOptions());
            // 调用模型生成响应
            ChatResponse response = modelContext.getChatModel().call(prompt);
            Generation generation = response.getResult();
            AssistantMessage output = generation.getOutput();
            metadata = response.getMetadata();

            // 解析助手消息段
            assistantSegments = resolveAssistantSegments(output);
            // 应用生成元数据到助手消息
            applyGenerationMetadata(assistantMessage, generation.getMetadata(), metadata);
            // 处理工具调用
            assistantMessage.setToolCalls(writeJson(output.getToolCalls()));
            // 设置消息类型
            assistantMessage.setMessageType(output.hasToolCalls()
                    ? ChatMessageType.TOOL_RESULT.getCode()
                    : ChatMessageType.NORMAL.getCode());
        } catch (Exception ex) {
            // 处理异常情况
            inferenceException = ex;
            assistantSegments = List.of(ChatMessageSegment.builder()
                    .type(ChatMessageType.ERROR.getCode().toLowerCase())
                    .text("对话生成失败: " + ex.getMessage())
                    .build());
            assistantMessage.setMessageType(ChatMessageType.ERROR.getCode());
            assistantMessage.setErrorMessage(ex.getMessage());
            assistantMessage.setStatus(0);
        }

        // 合并消息段文本，作为助手消息内容
        assistantMessage.setContentText(joinSegmentsText(assistantSegments));
        // 将消息段列表转换为 JSON 字符串，作为助手消息结构化内容
        assistantMessage.setContentJson(writeSegmentsJson(assistantSegments));
        // 计算推理延迟(毫秒)，至少为1毫秒
        assistantMessage.setLatencyMs(Math.max(1L, (System.nanoTime() - startAt) / 1_000_000));

        // 保存助手消息和统计
        Exception finalInferenceException = inferenceException;
        jdbcManager.executeInTransaction(jdbc -> {
            // 保存助手消息
            Long assistantId = chatMessageRepository.insert(assistantMessage, jdbc);
            assistantMessage.setId(assistantId);

            // 计算总令牌数(输入+输出)
            Integer totalTokens = safeAdd(assistantMessage.getInputTokens(), assistantMessage.getOutputTokens());
            // 更新会话统计
            chatSessionRepository.incrementStats(session.getId(), userId, 1, totalTokens, assistantMessage.getMessageTime(), null, jdbc);
            // 记录使用日志
            recordUsageLog(session, descriptor, assistantMessage, finalInferenceException, jdbc);
            return null;
        });

        // 构建附件映射
        Map<Long, List<ChatAttachment>> attachmentMap = new HashMap<>();
        attachmentMap.put(userMessage.getId(), userAttachments);
        attachmentMap.put(assistantMessage.getId(), List.of());

        // 构建响应DTO
        ChatMessageSendResponseDTO resp = new ChatMessageSendResponseDTO();
        resp.setSessionId(session.getId());
        resp.setNewSession(newSession);
        resp.setUserMessage(toResponse(userMessage, userAttachments));
        resp.setAssistantMessage(toResponse(assistantMessage, attachmentMap.get(assistantMessage.getId())));
        return resp;
    }

    /**
     * 流式发送消息
     *
     * @param request 发送请求
     * @param userId  用户ID
     * @return 流式事件流
     */
    @Override
    public Flux<ChatStreamEventDTO> stream(ChatMessageSendRequestDTO request, Long userId) {
        // 准备会话，如果没有消息则视为新会话
        ChatSession session = prepareSession(request, userId);
        boolean newSession = session.getMessageCount() == null || session.getMessageCount() == 0;

        // 构建用户消息
        ChatMessage userMessage = buildUserMessage(request, session, userId);
        // 构建用户附件
        List<ChatAttachment> userAttachments = buildAttachments(request.attachments(), userMessage, userId);

        // 保存用户消息和附件
        jdbcManager.executeInTransaction(jdbc -> {
            // 保存用户消息
            Long id = chatMessageRepository.insert(userMessage, jdbc);
            userMessage.setId(id);

            // 保存用户附件
            if (!userAttachments.isEmpty()) {
                userAttachments.forEach(att -> att.setMessageId(id));
                chatAttachmentRepository.batchInsert(userAttachments, jdbc);
            }

            // 更新会话统计
            chatSessionRepository.incrementStats(session.getId(), userId, 1, null,
                    userMessage.getMessageTime(), summarizeSessionTitle(session, userMessage.getContentText()), jdbc);

            return null;
        });

        // 查询会话最近消息(按时间倒序)
        List<ChatMessage> history = chatMessageRepository.listRecent(session.getId(), CONTEXT_MESSAGE_LIMIT);
        // 构建模型上下文
        ChatModelContext modelContext = aiAgentService.buildRuntimeContext(session.getAgentId(), userId);
        // 获取模型描述符
        ChatModelDescriptor descriptor = modelContext.getDescriptor();
        // 构建模型提示
        Prompt prompt = new Prompt(buildPromptMessages(descriptor.getAgent(), history), modelContext.getChatOptions());

        // 初始化流式累加器
        StreamAccumulator accumulator = new StreamAccumulator(session, userMessage, descriptor, userId, newSession);
        // 构建流式事件流头，包含会话ID、是否新会话、是否完成标志
        Flux<ChatStreamEventDTO> head = Flux.just(ChatStreamEventDTO.builder()
                .event("session")
                .sessionId(session.getId())
                .newSession(newSession)
                .finished(false)
                .build());

        // 调用模型流式API，处理每个响应块
        Flux<ChatStreamEventDTO> body = modelContext.getChatModel().stream(prompt)
                .map(response -> handleChunk(accumulator, response))
                .filter(Objects::nonNull);

        // 构建流式事件流尾，包含最终消息和完成标志
        Flux<ChatStreamEventDTO> tail = Flux.defer(() -> Flux.just(finalizeStream(accumulator)));

        // 合并头、体、尾流，处理错误情况
        return head.concatWith(body).concatWith(tail)
                .onErrorResume(ex -> Flux.just(handleStreamError(accumulator, ex)));
    }

    /**
     * 准备会话
     *
     * @param request 发送请求
     * @param userId  用户ID
     * @return 会话
     */
    private ChatSession prepareSession(ChatMessageSendRequestDTO request, Long userId) {
        // 如果有会话ID，检查会话是否存在
        if (request.sessionId() != null) {
            ChatSession exist = chatSessionRepository
                    .findById(request.sessionId(), userId)
                    .orElseThrow(() -> new BusinessException("会话不存在或已删除"));

            // 如果会话没有绑定智能体，且请求中提供了智能体ID，则绑定智能体
            if (exist.getAgentId() == null && request.agentId() != null) {
                exist.setAgentId(request.agentId());
            }

            return exist;
        }

        // 如果没有会话ID，创建新会话，要求提供智能体ID
        if (request.agentId() == null) {
            throw new BusinessException("agentId 不能为空");
        }

        // 创建新会话
        ChatSession newSession = ChatSession.builder()
                .sessionCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .userId(userId)
                .agentId(request.agentId())
                .title(trimToNull(extractTitle(request.content())))
                .summary(null)
                .pinned(0)
                .archived(0)
                .messageCount(0)
                .tokenCount(0)
                .lastMessageTime(null)
                .status(1)
                .createBy(userId)
                .createTime(LocalDateTime.now())
                .updateBy(userId)
                .updateTime(LocalDateTime.now())
                .deleted(0)
                .build();

        // 保存新会话
        jdbcManager.executeInTransaction(jdbc -> {
            long id = chatSessionRepository.insert(newSession, jdbc);
            newSession.setId(id);
            return null;
        });

        return newSession;
    }

    /**
     * 构建用户消息
     *
     * @param request 发送请求
     * @param session 会话
     * @param userId  用户ID
     * @return 用户消息
     */
    private ChatMessage buildUserMessage(ChatMessageSendRequestDTO request, ChatSession session, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        // 构建消息内容段
        List<ChatMessageSegment> segments = StringUtils.hasText(request.content())
                ? List.of(ChatMessageSegment.builder().type("text").text(request.content()).build())
                : List.of();

        String format = trimToNull(request.contentFormat());
        format = format == null ? ContentFormat.TEXT.getCode() : format.trim().toUpperCase();

        return ChatMessage.builder()
                .sessionId(session.getId())
                .parentMessageId(request.parentMessageId())
                .role(MessageRole.USER.getCode())
                .messageType(ChatMessageType.NORMAL.getCode())
                .contentText(request.content())
                .contentJson(writeSegmentsJson(segments))
                .contentFormat(format)
                .attachmentsCount(CollectionUtils.isEmpty(request.attachments()) ? 0 : request.attachments().size())
                .status(1)
                .messageTime(now)
                .createBy(userId)
                .createTime(now)
                .updateBy(userId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 构建附件
     *
     * @param payloads 附件有效载荷
     * @param owner    消息所有者
     * @param userId   用户ID
     * @return 附件列表
     */
    private List<ChatAttachment> buildAttachments(List<ChatAttachmentPayload> payloads, ChatMessage owner, Long userId) {
        if (CollectionUtils.isEmpty(payloads)) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<ChatAttachment> attachments = new ArrayList<>();

        for (ChatAttachmentPayload payload : payloads) {
            if (payload == null || !StringUtils.hasText(payload.uri())) {
                continue;
            }

            attachments.add(ChatAttachment.builder()
                    .messageId(owner.getId())
                    .fileId(payload.fileId())
                    .fileName(trimToNull(payload.fileName()))
                    .uri(payload.uri().trim())
                    .storageProvider(trimToNull(payload.storageProvider()))
                    .mediaType(trimToNull(payload.mediaType()))
                    .mimeType(trimToNull(payload.mimeType()))
                    .sizeBytes(payload.sizeBytes())
                    .width(payload.width())
                    .height(payload.height())
                    .durationMs(payload.durationMs())
                    .transcriptText(trimToNull(payload.transcriptText()))
                    .kbDocId(payload.kbDocId())
                    .kbChunkId(payload.kbChunkId())
                    .externalVectorId(trimToNull(payload.externalVectorId()))
                    .metadata(writeJson(payload.metadata()))
                    .status(1)
                    .createBy(userId)
                    .createTime(now)
                    .updateBy(userId)
                    .updateTime(now)
                    .deleted(0)
                    .build());
        }
        return attachments;
    }

    /**
     * 构建提示消息
     *
     * @param agent   智能体
     * @param history 消息历史
     * @return 提示消息列表
     */
    private List<Message> buildPromptMessages(AiAgent agent, List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();

        // 添加智能体提示
        if (agent != null && agent.getPromptId() != null) {
            Optional<AiPrompt> prompt = aiPromptRepository.findById(agent.getPromptId(), agent.getCreateBy());
            prompt.map(AiPrompt::getTemplate)
                    .filter(StringUtils::hasText)
                    .ifPresent(text -> messages.add(new SystemMessage(text)));
        }

        // 添加消息历史
        for (ChatMessage msg : history) {
            String text = flattenSegmentsForPrompt(msg);
            if (!StringUtils.hasText(text)) {
                continue;
            }

            if (MessageRole.SYSTEM.getCode().equalsIgnoreCase(msg.getRole())) {
                messages.add(new SystemMessage(text));
            } else if (MessageRole.ASSISTANT.getCode().equalsIgnoreCase(msg.getRole())) {
                messages.add(new AssistantMessage(text));
            } else {
                messages.add(new UserMessage(text));
            }
        }

        return messages;
    }

    /**
     * 为提示消息扁平化消息段
     *
     * @param message 消息
     * @return 扁平化文本
     */
    private String flattenSegmentsForPrompt(ChatMessage message) {
        List<ChatMessageSegment> segments = parseSegments(message.getContentJson(), message.getContentText());
        if (CollectionUtils.isEmpty(segments)) {
            return message.getContentText();
        }

        return segments.stream()
                .filter(segment -> !"reasoning".equalsIgnoreCase(segment.getType()))
                .map(ChatMessageSegment::getText)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining());
    }

    /**
     * 构建助手消息
     *
     * @param session     会话
     * @param userMessage 用户消息
     * @param descriptor  模型描述符
     * @param userId      用户ID
     * @return 助手消息
     */
    private ChatMessage baseAssistantMessage(ChatSession session, ChatMessage userMessage, ChatModelDescriptor descriptor, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return ChatMessage.builder()
                .sessionId(session.getId())
                .parentMessageId(userMessage.getId())
                .role(MessageRole.ASSISTANT.getCode())
                .messageType(ChatMessageType.NORMAL.getCode())
                .attachmentsCount(0)
                .contentFormat(ContentFormat.TEXT.getCode())
                .status(1)
                .providerId(descriptor.getProvider().getId())
                .modelId(descriptor.getModel().getId())
                .currency(Optional.ofNullable(descriptor.getModel().getCurrency()).orElse("USD"))
                .messageTime(now)
                .createBy(userId)
                .createTime(now)
                .updateBy(userId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 应用生成元数据到助手消息
     *
     * @param message            助手消息
     * @param generationMetadata 生成元数据
     * @param responseMetadata   响应元数据
     */
    private void applyGenerationMetadata(ChatMessage message, ChatGenerationMetadata generationMetadata, ChatResponseMetadata responseMetadata) {
        if (generationMetadata != null) {
            // 应用完成原因
            message.setFinishReason(generationMetadata.getFinishReason());
        }

        // 应用响应元数据
        if (responseMetadata != null) {
            Usage usage = responseMetadata.getUsage();
            if (usage != null) {
                // 应用输入令牌数
                message.setInputTokens(usage.getPromptTokens());
                // 应用输出令牌数
                message.setOutputTokens(usage.getCompletionTokens());
            }
        }
    }

    /**
     * 解析助手消息段
     *
     * @param message 助手消息
     * @return 消息段列表
     */
    private List<ChatMessageSegment> resolveAssistantSegments(AssistantMessage message) {
        List<ChatMessageSegment> segments = new ArrayList<>();
        Map<String, Object> metadata = message.getMetadata();
        extractReasoningSegments(segments, metadata);
        if (StringUtils.hasText(message.getText())) {
            segments.add(ChatMessageSegment.builder()
                    .type("text")
                    .text(message.getText())
                    .build());
        }
        return segments;
    }

    /**
     * 提取推理段
     *
     * @param segments 推理段列表
     * @param metadata 元数据
     */
    private void extractReasoningSegments(List<ChatMessageSegment> segments, Map<String, Object> metadata) {
        if (metadata == null) {
            return;
        }
        metadata.forEach((key, value) -> {
            if (key == null) {
                return;
            }
            String lower = key.toLowerCase();
            if (lower.contains("thinking") || lower.contains("reason")) {
                appendReasoningSegment(segments, value, metadata);
            }
        });
    }

    /**
     * 追加推理段
     *
     * @param segments 推理段列表
     * @param value    值
     * @param metadata 元数据
     */
    private void appendReasoningSegment(List<ChatMessageSegment> segments, Object value, Map<String, Object> metadata) {
        if (value == null) {
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                appendReasoningSegment(segments, item, metadata);
            }
            return;
        }
        String text;
        if (value instanceof Map) {
            text = jsonUtils.toJson(value);
        } else {
            text = value.toString();
        }
        if (!StringUtils.hasText(text)) {
            return;
        }
        segments.add(ChatMessageSegment.builder()
                .type("reasoning")
                .text(text)
                .metadata(metadata)
                .build());
    }

    /**
     * 处理流事件
     *
     * @param accumulator 流累加器
     * @param response    响应
     * @return 流事件
     */
    private ChatStreamEventDTO handleChunk(StreamAccumulator accumulator, ChatResponse response) {
        if (response == null) {
            return null;
        } else {
            response.getResult();
        }

        Generation generation = response.getResult();
        AssistantMessage output = generation.getOutput();
        accumulator.lastAssistant = output;
        accumulator.responseMetadata = response.getMetadata();
        accumulator.generationMetadata = generation.getMetadata();
        String delta = output.getText();
        if (!StringUtils.hasText(delta)) {
            return null;
        }
        String segmentType = resolveSegmentType(output);
        appendBuffer(accumulator, segmentType, delta, cloneMetadata(output.getMetadata()));

        return ChatStreamEventDTO.builder()
                .event("delta")
                .sessionId(accumulator.session.getId())
                .delta(delta)
                .segmentType(segmentType)
                .finished(false)
                .build();
    }

    /**
     * 最终化流
     *
     * @param accumulator 流累加器
     * @return 流事件
     */
    private ChatStreamEventDTO finalizeStream(StreamAccumulator accumulator) {
        ChatMessage assistant = baseAssistantMessage(accumulator.session, accumulator.userMessage, accumulator.descriptor, accumulator.userId);
        List<ChatMessageSegment> segments = mergeBuffers(accumulator);
        if (segments.isEmpty() && accumulator.lastAssistant != null) {
            segments = resolveAssistantSegments(accumulator.lastAssistant);
        }

        assistant.setContentText(joinSegmentsText(segments));
        assistant.setContentJson(writeSegmentsJson(segments));
        assistant.setLatencyMs(Math.max(1L, (System.nanoTime() - accumulator.startNano) / 1_000_000));
        applyGenerationMetadata(assistant, accumulator.generationMetadata, accumulator.responseMetadata);

        jdbcManager.executeInTransaction(jdbc -> {
            Long id = chatMessageRepository.insert(assistant, jdbc);
            assistant.setId(id);
            Integer totalTokens = safeAdd(assistant.getInputTokens(), assistant.getOutputTokens());
            chatSessionRepository.incrementStats(accumulator.session.getId(), accumulator.userId, 1, totalTokens, assistant.getMessageTime(), null, jdbc);
            recordUsageLog(accumulator.session, accumulator.descriptor, assistant, null, jdbc);
            return null;
        });

        ChatMessageResponseDTO dto = toResponse(assistant, List.of());
        return ChatStreamEventDTO.builder()
                .event("complete")
                .sessionId(accumulator.session.getId())
                .newSession(accumulator.newSession)
                .finished(true)
                .message(dto)
                .metadata(buildUsageMetadata(accumulator.responseMetadata))
                .build();
    }

    /**
     * 处理流错误
     *
     * @param accumulator 流累加器
     * @param error       错误
     * @return 流事件
     */
    private ChatStreamEventDTO handleStreamError(StreamAccumulator accumulator, Throwable error) {
        Exception ex = error instanceof Exception exception ? exception : new RuntimeException(error);

        ChatMessage assistant = baseAssistantMessage(accumulator.session, accumulator.userMessage, accumulator.descriptor, accumulator.userId);
        assistant.setMessageType(ChatMessageType.ERROR.getCode());
        assistant.setStatus(0);
        assistant.setErrorMessage(ex.getMessage());
        List<ChatMessageSegment> segments = List.of(ChatMessageSegment.builder()
                .type("error")
                .text("对话生成失败: " + ex.getMessage())
                .build());
        assistant.setContentText(joinSegmentsText(segments));
        assistant.setContentJson(writeSegmentsJson(segments));
        assistant.setLatencyMs(Math.max(1L, (System.nanoTime() - accumulator.startNano) / 1_000_000));

        jdbcManager.executeInTransaction(jdbc -> {
            Long id = chatMessageRepository.insert(assistant, jdbc);
            assistant.setId(id);
            chatSessionRepository.incrementStats(accumulator.session.getId(), accumulator.userId, 1, null, assistant.getMessageTime(), null, jdbc);
            recordUsageLog(accumulator.session, accumulator.descriptor, assistant, ex, jdbc);
            return null;
        });

        ChatMessageResponseDTO dto = toResponse(assistant, List.of());
        return ChatStreamEventDTO.builder()
                .event("error")
                .sessionId(accumulator.session.getId())
                .newSession(accumulator.newSession)
                .finished(true)
                .message(dto)
                .build();
    }

    /**
     * 追加缓冲区
     *
     * @param accumulator 流累加器
     * @param type        消息段类型
     * @param delta       消息段内容
     * @param metadata    消息段元数据
     */
    private void appendBuffer(StreamAccumulator accumulator, String type, String delta, Map<String, Object> metadata) {
        if (!StringUtils.hasText(delta)) {
            return;
        }

        SegmentBuffer last = accumulator.buffers.isEmpty() ? null : accumulator.buffers.getLast();
        if (last != null && Objects.equals(last.type, type)) {
            last.text.append(delta);
            return;
        }

        SegmentBuffer buffer = new SegmentBuffer(type, metadata);
        buffer.text.append(delta);
        accumulator.buffers.add(buffer);
    }

    /**
     * 合并缓冲区
     *
     * @param accumulator 流累加器
     * @return 消息段列表
     */
    private List<ChatMessageSegment> mergeBuffers(StreamAccumulator accumulator) {
        if (accumulator.buffers.isEmpty()) {
            return new ArrayList<>();
        }

        return accumulator.buffers.stream()
                .map(buffer -> ChatMessageSegment.builder()
                        .type(buffer.type)
                        .text(buffer.text.toString())
                        .metadata(buffer.metadata)
                        .build())
                .toList();
    }

    /**
     * 构建使用量元数据
     *
     * @param metadata 响应元数据
     * @return 使用量元数据
     */
    private Map<String, Object> buildUsageMetadata(ChatResponseMetadata metadata) {
        if (metadata == null || metadata.getUsage() == null) {
            return Map.of();
        }

        Usage usage = metadata.getUsage();
        Map<String, Object> map = new HashMap<>();
        map.put("promptTokens", usage.getPromptTokens());
        map.put("completionTokens", usage.getCompletionTokens());
        map.put("totalTokens", usage.getTotalTokens());
        return map;
    }

    /**
     * 解析消息段类型
     *
     * @param message 助手消息
     * @return 消息段类型
     */
    private String resolveSegmentType(AssistantMessage message) {
        Map<String, Object> metadata = message.getMetadata();
        if (MapUtils.isNotEmpty(metadata)) {
            Object type = metadata.get("type");
            if (type != null && type.toString().toLowerCase().contains("think")) {
                return "reasoning";
            }
            if (metadata.containsKey("thinking") || metadata.containsKey("reasoning")) {
                return "reasoning";
            }
        }
        return "text";
    }

    /**
     * 克隆消息元数据
     *
     * @param metadata 元数据
     * @return 克隆后的元数据
     */
    private Map<String, Object> cloneMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(metadata);
    }

    /**
     * 将 ChatMessage 实体转换为 DTO
     *
     * @param entity      ChatMessage 实体
     * @param attachments 附件列表
     * @return DTO
     */
    private ChatMessageResponseDTO toResponse(ChatMessage entity, List<ChatAttachment> attachments) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setId(entity.getId());
        dto.setSessionId(entity.getSessionId());
        dto.setParentMessageId(entity.getParentMessageId());
        dto.setRole(entity.getRole());
        dto.setMessageType(entity.getMessageType());
        dto.setContentFormat(entity.getContentFormat());
        List<ChatMessageSegment> segments = parseSegments(entity.getContentJson(), entity.getContentText());
        dto.setSegments(toSegmentDTOs(segments));
        dto.setContent(joinSegmentsText(segments));
        dto.setContentJson(readJsonMap(entity.getContentJson()));
        dto.setToolCalls(readJsonMap(entity.getToolCalls()));
        dto.setToolResults(readJsonMap(entity.getToolResults()));
        dto.setAttachments(toAttachmentResponses(attachments));
        dto.setProviderId(entity.getProviderId());
        dto.setModelId(entity.getModelId());
        dto.setFinishReason(entity.getFinishReason());
        dto.setInputTokens(entity.getInputTokens());
        dto.setOutputTokens(entity.getOutputTokens());
        dto.setLatencyMs(entity.getLatencyMs());
        dto.setCost(entity.getCost());
        dto.setCurrency(entity.getCurrency());
        dto.setErrorCode(entity.getErrorCode());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setMessageTime(entity.getMessageTime());
        return dto;
    }

    /**
     * 将附件列表转换为 DTO 列表
     *
     * @param attachments 附件列表
     * @return DTO 列表
     */
    private List<ChatAttachmentResponseDTO> toAttachmentResponses(List<ChatAttachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return List.of();
        }

        return attachments.stream().map(att -> {
            ChatAttachmentResponseDTO dto = new ChatAttachmentResponseDTO();
            dto.setId(att.getId());
            dto.setMessageId(att.getMessageId());
            dto.setFileId(att.getFileId());
            dto.setFileName(att.getFileName());
            dto.setUri(att.getUri());
            dto.setStorageProvider(att.getStorageProvider());
            dto.setMediaType(att.getMediaType());
            dto.setMimeType(att.getMimeType());
            dto.setSizeBytes(att.getSizeBytes());
            dto.setWidth(att.getWidth());
            dto.setHeight(att.getHeight());
            dto.setDurationMs(att.getDurationMs());
            dto.setTranscriptText(att.getTranscriptText());
            dto.setKbDocId(att.getKbDocId());
            dto.setKbChunkId(att.getKbChunkId());
            dto.setExternalVectorId(att.getExternalVectorId());
            dto.setMetadata(readJsonMap(att.getMetadata()));
            return dto;
        }).toList();
    }

    /**
     * 解析消息段 JSON 字符串
     *
     * @param json    JSON 字符串
     * @param fallback 回退文本
     * @return 消息段列表
     */
    private List<ChatMessageSegment> parseSegments(String json, String fallback) {
        if (StringUtils.hasText(json)) {
            try {
                return jsonUtils.fromJson(json, SEGMENT_LIST_TYPE);
            } catch (Exception ignored) {
                // ignore
            }
        }

        if (StringUtils.hasText(fallback)) {
            return List.of(ChatMessageSegment.builder()
                    .type("text")
                    .text(fallback)
                    .build());
        }

        return new ArrayList<>();
    }

    /**
     * 将消息段列表转换为 DTO 列表
     *
     * @param segments 消息段列表
     * @return DTO 列表
     */
    private List<ChatMessageSegmentDTO> toSegmentDTOs(List<ChatMessageSegment> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return List.of();
        }

        return segments.stream()
                .map(seg -> ChatMessageSegmentDTO.builder()
                        .type(seg.getType())
                        .text(seg.getText())
                        .metadata(seg.getMetadata())
                        .build())
                .toList();
    }

    /**
     * 合并消息段文本
     *
     * @param segments 消息段列表
     * @return 合并后的文本
     */
    private String joinSegmentsText(List<ChatMessageSegment> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return null;
        }

        return segments.stream()
                .map(ChatMessageSegment::getText)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining());
    }

    /**
     * 将消息段列表转换为 JSON 字符串
     *
     * @param segments 消息段列表
     * @return JSON 字符串
     */
    private String writeSegmentsJson(List<ChatMessageSegment> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return null;
        }
        return jsonUtils.toJson(segments);
    }

    /**
     * 将 JSON 字符串转换为 Map 对象
     *
     * @param json JSON 字符串
     * @return Map 对象
     */
    private Map<String, Object> readJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        return jsonUtils.fromJson(json, MAP_TYPE);
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param value 要转换的对象
     * @return JSON 字符串
     */
    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str && !StringUtils.hasText(str)) {
            return null;
        }
        return jsonUtils.toJson(value);
    }

    /**
     * 记录 AI 模型使用日志
     *
     * @param session       聊天会话(窗口)
     * @param descriptor    模型描述符
     * @param assistantMessage 助手消息
     * @param ex            异常(如果有)
     * @param jdbcTx        JDBC 事务管理器
     */
    private void recordUsageLog(ChatSession session, ChatModelDescriptor descriptor,
                                ChatMessage assistantMessage, Exception ex,
                                JdbcTemplateManager jdbcTx) {
        AiModel model = descriptor.getModel();
        AiUsageLog log = AiUsageLog.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(session.getUserId())
                .sessionId(session.getId())
                .providerId(descriptor.getProvider().getId())
                .modelId(model.getId())
                .modelKey(model.getModelKey())
                .operation(UsageOperation.CHAT.getCode())
                .inputTokens(assistantMessage.getInputTokens())
                .outputTokens(assistantMessage.getOutputTokens())
                .cost(calculateCost(model, assistantMessage.getInputTokens(), assistantMessage.getOutputTokens()))
                .currency(Optional.ofNullable(model.getCurrency()).orElse("USD"))
                .success(ex == null ? 1 : 0)
                .httpStatus(null)
                .latencyMs(assistantMessage.getLatencyMs())
                .createTime(LocalDateTime.now())
                .build();
        aiUsageLogRepository.insert(log, jdbcTx);
    }

    /**
     * 计算模型成本(单位: 美元)
     *
     * @param model        模型
     * @param inputTokens  输入令牌数
     * @param outputTokens 输出令牌数
     * @return 模型成本(如果成本为 0 则返回 null)
     */
    private BigDecimal calculateCost(AiModel model, Integer inputTokens, Integer outputTokens) {
        BigDecimal total = BigDecimal.ZERO;
        // 计算输入成本
        if (model.getPriceInputPer1k() != null && inputTokens != null) {
            BigDecimal input = model.getPriceInputPer1k()
                    .multiply(BigDecimal.valueOf(inputTokens))
                    .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP);
            total = total.add(input);
        }
        // 计算输出成本
        if (model.getPriceOutputPer1k() != null && outputTokens != null) {
            BigDecimal output = model.getPriceOutputPer1k()
                    .multiply(BigDecimal.valueOf(outputTokens))
                    .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP);
            total = total.add(output);
        }
        // 返回成本(如果成本为 0 则返回 null)
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : null;
    }

    /**
     * 会话标题摘要器(如果会话标题为空,则从最新消息中提取)
     *
     * @param session       聊天会话(窗口)
     * @param latestContent 最新消息内容
     * @return 会话标题摘要
     */
    private String summarizeSessionTitle(ChatSession session, String latestContent) {
        if (StringUtils.hasText(session.getTitle())) {
            return null;
        }
        if (!StringUtils.hasText(latestContent)) {
            return null;
        }
        return latestContent.length() > 40 ? latestContent.substring(0, 40) : latestContent;
    }

    /**
     * 安全累加器(处理 null 值)
     *
     * @param a 加数
     * @param b 被加数
     * @return 累加结果(如果结果为 0 则返回 null)
     */
    private Integer safeAdd(Integer a, Integer b) {
        int left = a == null ? 0 : a;
        int right = b == null ? 0 : b;
        int sum = left + right;
        return sum == 0 ? null : sum;
    }

    /**
     * 从消息内容中提取会话标题
     *
     * @param content 消息内容
     * @return 会话标题
     */
    private String extractTitle(String content) {
        if (!StringUtils.hasText(content)) {
            return "新会话";
        }

        String trimmed = content.trim();
        return trimmed.length() > 30 ? trimmed.substring(0, 30) : trimmed;
    }

    /**
     * 流式处理累加器
     */
    private static class StreamAccumulator {

        /**
         * 聊天会话(窗口)
         */
        private final ChatSession session;

        /**
         * 用户消息
         */
        private final ChatMessage userMessage;

        /**
         * 模型描述符
         */
        private final ChatModelDescriptor descriptor;

        /**
         * 用户ID
         */
        private final Long userId;

        /**
         * 是否新会话
         */
        private final boolean newSession;

        /**
         * 开始时间(纳秒)
         */
        private final long startNano = System.nanoTime();

        /**
         * 消息缓冲区
         */
        private final List<SegmentBuffer> buffers = new ArrayList<>();

        /**
         * 响应元数据
         */
        private ChatResponseMetadata responseMetadata;

        /**
         * 生成元数据
         */
        private ChatGenerationMetadata generationMetadata;

        /**
         * 最后一条助手消息
         */
        private AssistantMessage lastAssistant;

        /**
         * 构造函数
         *
         * @param session     聊天会话(窗口)
         * @param userMessage 用户消息
         * @param descriptor  模型描述符
         * @param userId      用户ID
         * @param newSession  是否新会话
         */
        private StreamAccumulator(ChatSession session, ChatMessage userMessage, ChatModelDescriptor descriptor, Long userId, boolean newSession) {
            this.session = session;
            this.userMessage = userMessage;
            this.descriptor = descriptor;
            this.userId = userId;
            this.newSession = newSession;
        }
    }

    /**
     * 消息缓冲区(用于累加)
     */
    private static class SegmentBuffer {

        /**
         * 消息类型
         */
        private final String type;

        /**
         * 消息元数据
         */
        private final Map<String, Object> metadata;

        /**
         * 消息文本内容
         */
        private final StringBuilder text = new StringBuilder();

        /**
         * 构造函数
         *
         * @param type     消息类型
         * @param metadata 消息元数据
         */
        private SegmentBuffer(String type, Map<String, Object> metadata) {
            this.type = type;
            this.metadata = metadata;
        }
    }
}
