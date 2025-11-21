package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.chat.dto.request.ChatSessionCreateRequestDTO;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionPageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionUpdateRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatSessionResponseDTO;
import cn.refinex.ai.converter.ChatSessionConverter;
import cn.refinex.ai.entity.ChatSession;
import cn.refinex.ai.repository.ChatSessionRepository;
import cn.refinex.ai.service.ChatSessionService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * 会话服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    /**
     * 会话编码长度
     */
    private static final int SESSION_CODE_LENGTH = 16;

    private final ChatSessionRepository repository;
    private final ChatSessionConverter converter;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 分页查询会话
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    @Override
    public PageResponse<ChatSessionResponseDTO> page(ChatSessionPageRequest request, Long userId) {
        ChatSessionPageRequest query = Objects.isNull(request) ? new ChatSessionPageRequest() : request;
        PageResponse<ChatSession> page = repository.page(userId,
                query.getStatus(), query.getPinned(), query.getArchived(),
                trimToNull(query.getKeyword()), query);
        return page.map(converter::toResponse);
    }

    /**
     * 根据ID查询会话
     *
     * @param id     会话ID
     * @param userId 用户ID
     * @return 会话响应对象
     */
    @Override
    public Optional<ChatSessionResponseDTO> findById(Long id, Long userId) {
        return repository.findById(id, userId).map(converter::toResponse);
    }

    /**
     * 创建会话
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 会话响应对象
     */
    @Override
    public ChatSessionResponseDTO create(ChatSessionCreateRequestDTO request, Long userId) {
        ChatSession session = ChatSession.builder()
                .sessionCode(generateCode())
                .userId(userId)
                .agentId(request.agentId())
                .title(trimToNull(request.title()))
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

        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(session, jdbc);
            session.setId(id);
            return null;
        });

        return converter.toResponse(session);
    }

    /**
     * 更新会话
     *
     * @param id     会话ID
     * @param request 更新请求
     * @param userId  用户ID
     */
    @Override
    public void update(Long id, ChatSessionUpdateRequestDTO request, Long userId) {
        ChatSession exist = repository.findById(id, userId)
                .orElseThrow(() -> new BusinessException("会话不存在或已删除"));

        exist.setTitle(request.title() == null ? exist.getTitle() : trimToNull(request.title()));
        exist.setPinned(request.pinned() == null ? exist.getPinned() : normalizeFlag(request.pinned(), exist.getPinned()));
        exist.setArchived(request.archived() == null ? exist.getArchived() : normalizeFlag(request.archived(), exist.getArchived()));
        exist.setStatus(request.status() == null ? exist.getStatus() : normalizeFlag(request.status(), exist.getStatus()));
        exist.setUpdateBy(userId);
        exist.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(exist, jdbc);
            return null;
        });
    }

    /**
     * 删除会话(逻辑删除)
     *
     * @param id     会话ID
     * @param userId 用户ID
     */
    @Override
    public void delete(Long id, Long userId) {
        repository.findById(id, userId).orElseThrow(() -> new BusinessException("会话不存在或已删除"));
        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(id, userId, jdbc);
            return null;
        });
    }

    /**
     * 生成会话编码
     *
     * @return 会话编码
     */
    private String generateCode() {
        return RandomStringUtils.secure().nextAlphanumeric(SESSION_CODE_LENGTH).toLowerCase();
    }

    /**
     * 标准化标志位
     *
     * @param value        输入值
     * @param defaultValue 默认值
     * @return 标准化后的标志位
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
