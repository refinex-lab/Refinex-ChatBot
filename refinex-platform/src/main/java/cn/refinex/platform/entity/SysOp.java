package cn.refinex.platform.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统操作定义实体类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
//@Builder
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统操作定义实体类")
public class SysOp extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作编码")
    private String opCode;

    @Schema(description = "操作名称")
    private String opName;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否系统内置:1是,0否")
    private Integer builtin;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;
}
