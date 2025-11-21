package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.agent.dto.request.AiAgentCreateRequestDTO;
import cn.refinex.ai.controller.agent.dto.request.AiAgentPageRequest;
import cn.refinex.ai.controller.agent.dto.request.AiAgentUpdateRequestDTO;
import cn.refinex.ai.controller.agent.dto.response.AiAgentResponseDTO;
import cn.refinex.ai.converter.AiAgentConverter;
import cn.refinex.ai.core.factory.ChatModelFactory;
import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.model.ChatModelDescriptorAssembler;
import cn.refinex.ai.entity.*;
import cn.refinex.ai.repository.*;
import cn.refinex.ai.service.AiAgentService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * Agent 服务实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiAgentServiceImpl implements AiAgentService {

    private final AiAgentRepository agentRepository;
    private final AiModelRepository modelRepository;
    private final AiPromptRepository promptRepository;
    private final AiSchemaRepository schemaRepository;
    private final AiToolRepository toolRepository;
    private final AiAdvisorRepository advisorRepository;
    private final AiAgentConverter converter;
    private final JsonUtils jsonUtils;
    private final JdbcTemplateManager jdbcManager;
    private final ChatModelDescriptorAssembler descriptorAssembler;
    private final ChatModelFactory chatModelFactory;

    /**
     * 分页查询 Agent
     *
     * @param request 分页请求
     * @return Agent 列表
     */
    @Override
    public PageResponse<AiAgentResponseDTO> page(AiAgentPageRequest request) {
        AiAgentPageRequest query = Objects.isNull(request) ? new AiAgentPageRequest() : request;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiAgent> page = agentRepository.page(
                query.getModelId(), query.getProviderId(), query.getStatus(),
                trimToNull(query.getKeyword()), query, userId
        );
        return page.map(agent -> converter.toResponse(agent,
                agentRepository.listToolIds(agent.getId()),
                agentRepository.listAdvisorIds(agent.getId())));
    }

    /**
     * 根据 ID 查询 Agent
     *
     * @param id 主键
     * @return Agent
     */
    @Override
    public Optional<AiAgentResponseDTO> findById(Long id) {
        Long userId = LoginHelper.getUserId();
        return agentRepository.findById(id, userId)
                .map(agent -> converter.toResponse(
                        agent,
                        agentRepository.listToolIds(agent.getId()),
                        agentRepository.listAdvisorIds(agent.getId()))
                );
    }

    /**
     * 新增 Agent
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void create(AiAgentCreateRequestDTO request, Long operatorId) {
        String code = request.agentCode().trim();
        if (agentRepository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("Agent 编码已存在: " + code);
        }

        validateReferences(
                request.modelId(), request.promptId(), request.outputSchemaId(),
                request.toolIds(), request.advisorIds(), operatorId
        );

        AiAgent entity = buildEntity(
                request.agentCode(), request.agentName(), request.description(), request.modelId(),
                request.promptId(), request.outputSchemaId(), request.ragKbId(), request.temperature(),
                request.topP(), request.presencePenalty(), request.frequencyPenalty(), request.maxTokens(),
                request.stopSequences(), request.toolChoice(), request.status(), request.remark(), operatorId
        );

        LocalDateTime now = LocalDateTime.now();
        entity.setCreateBy(operatorId);
        entity.setCreateTime(now);
        entity.setUpdateBy(operatorId);
        entity.setUpdateTime(now);
        entity.setDeleted(0);

        List<AiAgentTool> tools = assembleTools(request.toolIds(), operatorId);
        List<AiAgentAdvisor> advisors = assembleAdvisors(request.advisorIds(), operatorId);

        jdbcManager.executeInTransaction(jdbc -> {
            long id = agentRepository.insert(entity, jdbc);
            entity.setId(id);
            agentRepository.replaceAgentTools(id, tools, jdbc);
            agentRepository.replaceAgentAdvisors(id, advisors, jdbc);
            return null;
        });
    }

    /**
     * 更新 Agent
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void update(Long id, AiAgentUpdateRequestDTO request, Long operatorId) {
        AiAgent exist = agentRepository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Agent 不存在或已删除"));

        String newCode = request.agentCode().trim();
        if (!exist.getAgentCode().equals(newCode)
                && agentRepository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("Agent 编码已存在: " + newCode);
        }

        validateReferences(
                request.modelId(), request.promptId(), request.outputSchemaId(),
                request.toolIds(), request.advisorIds(), operatorId
        );

        AiAgent entity = buildEntity(
                request.agentCode(), request.agentName(), request.description(), request.modelId(),
                request.promptId(), request.outputSchemaId(), request.ragKbId(), request.temperature(),
                request.topP(), request.presencePenalty(), request.frequencyPenalty(), request.maxTokens(),
                request.stopSequences(), request.toolChoice(), request.status(), request.remark(), operatorId
        );

        entity.setId(exist.getId());
        entity.setCreateBy(exist.getCreateBy());
        entity.setCreateTime(exist.getCreateTime());
        entity.setDeleted(exist.getDeleted());
        entity.setUpdateTime(LocalDateTime.now());

        List<AiAgentTool> tools = assembleTools(request.toolIds(), operatorId);
        List<AiAgentAdvisor> advisors = assembleAdvisors(request.advisorIds(), operatorId);

        jdbcManager.executeInTransaction(jdbc -> {
            agentRepository.update(entity, jdbc);
            agentRepository.replaceAgentTools(entity.getId(), tools, jdbc);
            agentRepository.replaceAgentAdvisors(entity.getId(), advisors, jdbc);
            return null;
        });
    }

    /**
     * 更新 Agent 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiAgent exist = agentRepository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Agent 不存在或已删除"));

        int normalized = normalizeFlag(status, exist.getStatus());
        if (Objects.equals(exist.getStatus(), normalized)) {
            return;
        }

        exist.setStatus(normalized);
        exist.setUpdateBy(operatorId);
        exist.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            agentRepository.update(exist, jdbc);
            return null;
        });
    }

    /**
     * 删除 Agent
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiAgent exist = agentRepository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Agent 不存在或已删除"));

        jdbcManager.executeInTransaction(jdbc -> {
            agentRepository.logicalDelete(exist.getId(), operatorId, jdbc);
            agentRepository.replaceAgentTools(exist.getId(), Collections.emptyList(), jdbc);
            agentRepository.replaceAgentAdvisors(exist.getId(), Collections.emptyList(), jdbc);
            return null;
        });
    }

    /**
     * 构建运行时上下文
     *
     * @param agentId    Agent 主键
     * @param operatorId 操作人 ID
     * @return 运行时上下文
     */
    @Override
    public ChatModelContext buildRuntimeContext(Long agentId, Long operatorId) {
        ChatModelDescriptor descriptor = descriptorAssembler.build(agentId, operatorId);
        return chatModelFactory.create(descriptor);
    }

    /**
     * 构建 Agent 实体
     *
     * @param agentCode    Agent 编码
     * @param agentName    Agent 名称
     * @param description  描述
     * @param modelId      模型 ID
     * @param promptId     Prompt ID
     * @param schemaId     输出 Schema ID
     * @param ragKbId      RAG KB ID
     * @param temperature  温度
     * @param topP         Top P
     * @param presencePenalty 存在惩罚
     * @param frequencyPenalty 频率惩罚
     * @param maxTokens    最大令牌数
     * @param stopSequences 停止序列
     * @param toolChoice   工具选择
     * @param status       状态
     * @param remark       备注
     * @param operatorId   操作人 ID
     * @return Agent 实体
     */
    private AiAgent buildEntity(String agentCode, String agentName, String description,
                                Long modelId, Long promptId, Long schemaId, Long ragKbId,
                                BigDecimal temperature, BigDecimal topP,
                                BigDecimal presencePenalty, BigDecimal frequencyPenalty,
                                Integer maxTokens, List<String> stopSequences, String toolChoice,
                                Integer status, String remark, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiAgent.builder()
                .agentCode(agentCode.trim())
                .agentName(agentName.trim())
                .description(trimToNull(description))
                .modelId(modelId)
                .promptId(promptId)
                .outputSchemaId(schemaId)
                .ragKbId(ragKbId)
                .temperature(temperature)
                .topP(topP)
                .presencePenalty(presencePenalty)
                .frequencyPenalty(frequencyPenalty)
                .maxTokens(normalizePositive(maxTokens))
                .stopSequences(toJson(stopSequences))
                .toolChoice(trimToNull(toolChoice))
                .status(normalizeFlag(status, 1))
                .remark(trimToNull(remark))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 将列表转换为 JSON 字符串
     *
     * @param values 列表值
     * @return JSON 字符串
     */
    private String toJson(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return jsonUtils.toJson(values);
    }

    /**
     * 归一化非负整数
     *
     * @param value 整数值
     * @return 归一化后的整数
     */
    private Integer normalizePositive(Integer value) {
        return value != null && value > 0 ? value : null;
    }

    /**
     * 归一化状态标志
     *
     * @param value       状态值
     * @param defaultValue 默认值
     * @return 归一化后的状态标志
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }

    /**
     * 验证引用完整性
     *
     * @param modelId    模型 ID
     * @param promptId   Prompt ID
     * @param schemaId   输出 Schema ID
     * @param toolIds    工具 ID 列表
     * @param advisorIds Advisor ID 列表
     * @param operatorId 操作人 ID
     */
    private void validateReferences(Long modelId, Long promptId, Long schemaId, List<Long> toolIds, List<Long> advisorIds, Long operatorId) {
        modelRepository
                .findById(modelId, operatorId)
                .orElseThrow(() -> new BusinessException("模型不存在或已删除"));

        if (promptId != null) {
            promptRepository
                    .findById(promptId, operatorId)
                    .orElseThrow(() -> new BusinessException("提示词不存在或已删除"));
        }
        if (schemaId != null) {
            schemaRepository
                    .findById(schemaId, operatorId)
                    .orElseThrow(() -> new BusinessException("Schema 不存在或已删除"));
        }
        if (!CollectionUtils.isEmpty(toolIds)) {
            List<AiTool> tools = toolRepository.findByIds(toolIds, operatorId);
            if (tools.size() != toolIds.size()) {
                throw new BusinessException("部分工具不存在或已删除");
            }
        }
        if (!CollectionUtils.isEmpty(advisorIds)) {
            List<AiAdvisor> advisors = advisorRepository.findByIds(advisorIds, operatorId);
            if (advisors.size() != advisorIds.size()) {
                throw new BusinessException("部分 Advisor 不存在或已删除");
            }
        }
    }

    /**
     * 组装工具关系
     *
     * @param toolIds    工具 ID 列表
     * @param operatorId 操作人 ID
     * @return 工具关系列表
     */
    private List<AiAgentTool> assembleTools(List<Long> toolIds, Long operatorId) {
        if (CollectionUtils.isEmpty(toolIds)) {
            return Collections.emptyList();
        }

        List<AiAgentTool> list = new ArrayList<>();
        int sort = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long toolId : toolIds) {
            if (toolId == null) {
                continue;
            }

            AiAgentTool rel = AiAgentTool.builder()
                    .toolId(toolId)
                    .sort(sort++)
                    .createBy(operatorId)
                    .createTime(now)
                    .build();
            list.add(rel);
        }
        return list;
    }

    /**
     * 组装 Advisor 关系
     *
     * @param advisorIds Advisor ID 列表
     * @param operatorId 操作人 ID
     * @return Advisor 关系列表
     */
    private List<AiAgentAdvisor> assembleAdvisors(List<Long> advisorIds, Long operatorId) {
        if (CollectionUtils.isEmpty(advisorIds)) {
            return Collections.emptyList();
        }

        List<AiAgentAdvisor> list = new ArrayList<>();
        int sort = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long advisorId : advisorIds) {
            if (advisorId == null) {
                continue;
            }

            AiAgentAdvisor rel = AiAgentAdvisor.builder()
                    .advisorId(advisorId)
                    .sort(sort++)
                    .createBy(operatorId)
                    .createTime(now)
                    .build();
            list.add(rel);
        }
        return list;
    }
}
