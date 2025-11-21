package cn.refinex.ai.converter;

import cn.refinex.ai.controller.agent.dto.response.AiAgentResponseDTO;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.json.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Agent 对象转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AiAgentConverter {

    /**
     * 字符串列表类型引用
     */
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final JsonUtils jsonUtils;

    /**
     * 转换为 Agent 响应
     *
     * @param entity    Agent 实体
     * @param toolIds   工具 ID 列表
     * @param advisorIds 顾问 ID 列表
     * @return Agent 响应
     */
    public AiAgentResponseDTO toResponse(AiAgent entity, List<Long> toolIds, List<Long> advisorIds) {
        AiAgentResponseDTO dto = new AiAgentResponseDTO();
        dto.setId(entity.getId());
        dto.setAgentCode(entity.getAgentCode());
        dto.setAgentName(entity.getAgentName());
        dto.setDescription(entity.getDescription());
        dto.setModelId(entity.getModelId());
        dto.setPromptId(entity.getPromptId());
        dto.setOutputSchemaId(entity.getOutputSchemaId());
        dto.setRagKbId(entity.getRagKbId());
        dto.setTemperature(entity.getTemperature());
        dto.setTopP(entity.getTopP());
        dto.setPresencePenalty(entity.getPresencePenalty());
        dto.setFrequencyPenalty(entity.getFrequencyPenalty());
        dto.setMaxTokens(entity.getMaxTokens());
        dto.setStopSequences(readList(entity.getStopSequences()));
        dto.setToolChoice(entity.getToolChoice());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setToolIds(toolIds);
        dto.setAdvisorIds(advisorIds);
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    /**
     * 读取 JSON 字符串为字符串列表
     *
     * @param json JSON 字符串
     * @return 字符串列表
     */
    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        return jsonUtils.fromJson(json, STRING_LIST);
    }
}
