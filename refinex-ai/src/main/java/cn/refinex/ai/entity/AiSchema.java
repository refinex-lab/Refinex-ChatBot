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
 * 结构化输出 Schema 定义
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "结构化输出 Schema 定义")
public class AiSchema extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "Schema 编码，唯一")
    private String schemaCode;

    @Schema(description = "Schema 名称")
    private String schemaName;

    @Schema(description = "Schema 类型: JSON_SCHEMA/PROTO/XML/YAML")
    private String schemaType;

    @Schema(description = "Schema JSON 定义")
    private String schemaJson;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "是否严格校验:1是,0否")
    private Integer strictMode;

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

