package cn.refinex.ai.controller.mcp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP Server 响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "MCP Server 响应对象")
public class AiMcpServerResponseDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "编码")
    private String serverCode;

    @Schema(description = "名称")
    private String serverName;

    @Schema(description = "传输类型")
    private String transportType;

    @Schema(description = "启动命令")
    private String entryCommand;

    @Schema(description = "网络端点")
    private String endpointUrl;

    @Schema(description = "Manifest URL")
    private String manifestUrl;

    @Schema(description = "鉴权类型")
    private String authType;

    @Schema(description = "鉴权密钥密文")
    private String authSecretCipher;

    @Schema(description = "鉴权密钥别名")
    private String authSecretIndex;

    @Schema(description = "工具白名单")
    private String toolsFilter;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
