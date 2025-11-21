package cn.refinex.ai.controller.schema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Schema 更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "Schema 更新请求")
public record AiSchemaUpdateRequestDTO(

        @NotBlank(message = "Schema 编码不能为空")
        @Size(max = 100, message = "Schema 编码长度不能超过 100 个字符")
        @Schema(description = "Schema 编码")
        String schemaCode,

        @NotBlank(message = "Schema 名称不能为空")
        @Size(max = 100, message = "Schema 名称长度不能超过 100 个字符")
        @Schema(description = "Schema 名称")
        String schemaName,

        @NotBlank(message = "Schema 类型不能为空")
        @Size(max = 30, message = "Schema 类型长度不能超过 30 个字符")
        @Schema(description = "Schema 类型")
        String schemaType,

        @Schema(description = "Schema JSON 定义")
        Map<String, Object> schemaJson,

        @Schema(description = "版本号")
        Integer version,

        @Schema(description = "严格模式:1是,0否")
        Integer strictMode,

        @Schema(description = "状态")
        Integer status,

        @Schema(description = "备注")
        String remark
) {
}
