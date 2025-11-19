package cn.refinex.ai.converter;

import cn.refinex.ai.controller.prompt.dto.response.AiPromptResponseDTO;
import cn.refinex.ai.entity.AiPrompt;
import cn.refinex.json.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 提示词对象转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AiPromptConverter {

    /**
     * 提示词变量 JSON 映射
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    /**
     * 提示词示例 JSON 映射
     */
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {
    };

    @Autowired
    protected JsonUtils jsonUtils;

    @Mapping(target = "variables", expression = "java(readMap(entity.getVariables()))")
    @Mapping(target = "examples", expression = "java(readList(entity.getExamples()))")
    @Mapping(target = "inputSchema", expression = "java(readMap(entity.getInputSchema()))")
    public abstract AiPromptResponseDTO toResponse(AiPrompt entity);

    /**
     * 读取提示词变量 JSON 映射
     *
     * @param json 提示词变量 JSON 字符串
     * @return 提示词变量映射
     */
    protected Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        return jsonUtils.fromJson(json, MAP_TYPE);
    }

    /**
     * 读取提示词示例 JSON 映射
     *
     * @param json 提示词示例 JSON 字符串
     * @return 提示词示例映射列表
     */
    protected List<Map<String, Object>> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        return jsonUtils.fromJson(json, LIST_TYPE);
    }
}
