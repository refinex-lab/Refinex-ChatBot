package cn.refinex.ai.converter;

import cn.refinex.ai.controller.schema.dto.response.AiSchemaResponseDTO;
import cn.refinex.ai.entity.AiSchema;
import cn.refinex.json.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Schema 转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AiSchemaConverter {

    /**
     * 字符串映射类型引用
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JsonUtils jsonUtils;

    /**
     * 转换为 Schema 响应
     *
     * @param entity Schema 实体
     * @return Schema 响应
     */
    public AiSchemaResponseDTO toResponse(AiSchema entity) {
        AiSchemaResponseDTO dto = new AiSchemaResponseDTO();
        dto.setId(entity.getId());
        dto.setSchemaCode(entity.getSchemaCode());
        dto.setSchemaName(entity.getSchemaName());
        dto.setSchemaType(entity.getSchemaType());
        dto.setSchemaJson(readMap(entity.getSchemaJson()));
        dto.setVersion(entity.getVersion());
        dto.setStrictMode(entity.getStrictMode());
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
