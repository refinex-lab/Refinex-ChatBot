package cn.refinex.ai.controller.tool.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 工具更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "工具更新请求")
public record AiToolUpdateRequestDTO(

        @NotBlank(message = "工具编码不能为空")
        @Size(max = 50, message = "工具编码长度不能超过 50 个字符")
        @Schema(description = "工具编码")
        String toolCode,

        @NotBlank(message = "工具名称不能为空")
        @Size(max = 100, message = "工具名称长度不能超过 100 个字符")
        @Schema(description = "工具名称")
        String toolName,

        @NotBlank(message = "工具类型不能为空")
        @Size(max = 30, message = "工具类型长度不能超过 30 个字符")
        @Schema(description = "工具类型")
        String toolType,

        @Schema(description = "实现 Bean 名")
        String implBean,

        @Schema(description = "HTTP/脚本端点")
        String endpoint,

        @Schema(description = "超时时间(ms)")
        Integer timeoutMs,

        @Schema(description = "输入 Schema(JSON)")
        Map<String, Object> inputSchema,

        @Schema(description = "输出 Schema(JSON)")
        Map<String, Object> outputSchema,

        @Schema(description = "MCP Server ID")
        Long mcpServerId,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Schema(description = "备注")
        String remark
) {
}
