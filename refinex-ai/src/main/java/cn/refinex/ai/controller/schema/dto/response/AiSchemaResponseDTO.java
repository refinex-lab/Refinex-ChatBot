package cn.refinex.ai.controller.schema.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Schema 响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "Schema 响应对象")
public class AiSchemaResponseDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "Schema 编码")
    private String schemaCode;

    @Schema(description = "Schema 名称")
    private String schemaName;

    @Schema(description = "Schema 类型")
    private String schemaType;

    @Schema(description = "Schema JSON 定义")
    private Map<String, Object> schemaJson;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "严格模式")
    private Integer strictMode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
