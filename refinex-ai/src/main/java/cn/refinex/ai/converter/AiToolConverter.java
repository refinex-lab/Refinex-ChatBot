package cn.refinex.ai.converter;

import cn.refinex.ai.controller.tool.dto.response.AiToolResponseDTO;
import cn.refinex.ai.entity.AiTool;
import cn.refinex.json.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 工具转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AiToolConverter {

    /**
     * 字符串映射类型引用
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JsonUtils jsonUtils;

    /**
     * 转换为 Tool 响应
     *
     * @param entity Tool 实体
     * @return Tool 响应
     */
    public AiToolResponseDTO toResponse(AiTool entity) {
        AiToolResponseDTO dto = new AiToolResponseDTO();
        dto.setId(entity.getId());
        dto.setToolCode(entity.getToolCode());
        dto.setToolName(entity.getToolName());
        dto.setToolType(entity.getToolType());
        dto.setImplBean(entity.getImplBean());
        dto.setEndpoint(entity.getEndpoint());
        dto.setTimeoutMs(entity.getTimeoutMs());
        dto.setInputSchema(readMap(entity.getInputSchema()));
        dto.setOutputSchema(readMap(entity.getOutputSchema()));
        dto.setMcpServerId(entity.getMcpServerId());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    /**
     * 读取 JSON 字符串为字符串映射
     *
     * @param json JSON 字符串
     * @return 字符串映射
     */
    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        return jsonUtils.fromJson(json, MAP_TYPE);
    }
}
