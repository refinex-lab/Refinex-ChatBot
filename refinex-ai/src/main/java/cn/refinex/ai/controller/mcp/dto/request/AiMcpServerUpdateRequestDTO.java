package cn.refinex.ai.controller.mcp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * MCP Server 更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "MCP Server 更新请求")
public record AiMcpServerUpdateRequestDTO(

        @NotBlank(message = "Server 编码不能为空")
        @Size(max = 50, message = "Server 编码长度不能超过 50 个字符")
        @Schema(description = "Server 编码")
        String serverCode,

        @NotBlank(message = "Server 名称不能为空")
        @Size(max = 100, message = "Server 名称长度不能超过 100 个字符")
        @Schema(description = "Server 名称")
        String serverName,

        @NotBlank(message = "传输类型不能为空")
        @Size(max = 30, message = "传输类型长度不能超过 30 个字符")
        @Schema(description = "传输类型")
        String transportType,

        @Schema(description = "启动命令")
        String entryCommand,

        @Schema(description = "网络端点")
        String endpointUrl,

        @Schema(description = "能力清单 URL")
        String manifestUrl,

        @Schema(description = "鉴权类型")
        String authType,

        @Schema(description = "鉴权密钥密文")
        String authSecretCipher,

        @Schema(description = "鉴权密钥索引/别名")
        String authSecretIndex,

        @Schema(description = "工具白名单，逗号分隔")
        String toolsFilter,

        @Schema(description = "状态")
        Integer status,

        @Schema(description = "备注")
        String remark
) {
}
