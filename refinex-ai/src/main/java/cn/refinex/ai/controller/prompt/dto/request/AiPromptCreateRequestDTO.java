package cn.refinex.ai.controller.prompt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * 创建提示词请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "创建提示词请求")
public record AiPromptCreateRequestDTO(

        @NotBlank(message = "提示词编码不能为空")
        @Size(max = 100, message = "提示词编码长度不能超过100")
        @Schema(description = "提示词编码，唯一")
        String promptCode,

        @NotBlank(message = "提示词名称不能为空")
        @Size(max = 100, message = "提示词名称长度不能超过100")
        @Schema(description = "提示词名称")
        String promptName,

        @Schema(description = "分类: general/rag/agent/test")
        @Size(max = 50, message = "分类长度不能超过50")
        String category,

        @Schema(description = "描述说明")
        @Size(max = 500, message = "描述长度不能超过500")
        String description,

        @Schema(description = "模板格式: SPRING/MUSTACHE/STRING/LITERAL, 默认SPRING")
        @Size(max = 30, message = "模板格式长度不能超过30")
        String templateFormat,

        @Schema(description = "默认角色: system/user/assistant/tool")
        @Size(max = 20, message = "角色长度不能超过20")
        String role,

        @NotBlank(message = "提示词模板内容不能为空")
        @Schema(description = "提示词模板正文")
        String template,

        @Schema(description = "变量示例/默认值(JSON)")
        Map<String, Object> variables,

        @Schema(description = "Few-Shot 示例(JSON)")
        List<Map<String, Object>> examples,

        @Schema(description = "输入参数 JSON-Schema")
        Map<String, Object> inputSchema,

        @Schema(description = "状态:1启用,0停用, 默认1")
        Integer status,

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过500")
        String remark
) {
}
