package cn.refinex.ai.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MCP Server 定义
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP Server 定义")
public class AiMcpServer extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "MCP Server 编码")
    private String serverCode;

    @Schema(description = "MCP Server 名称")
    private String serverName;

    @Schema(description = "传输: stdio/sse/ws/http")
    private String transportType;

    @Schema(description = "启动命令或可执行路径(自托管)")
    private String entryCommand;

    @Schema(description = "网络端点(远程)")
    private String endpointUrl;

    @Schema(description = "清单/能力发现 URL")
    private String manifestUrl;

    @Schema(description = "鉴权类型: NONE/BEARER/BASIC")
    private String authType;

    @Schema(description = "鉴权密钥密文(AES-GCM)")
    private String authSecretCipher;

    @Schema(description = "鉴权密钥索引/别名")
    private String authSecretIndex;

    @Schema(description = "工具白名单(逗号分隔)，为空表示全部可用")
    private String toolsFilter;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注")
    private String remark;
}

