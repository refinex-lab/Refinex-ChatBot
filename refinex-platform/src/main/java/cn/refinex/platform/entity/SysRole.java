package cn.refinex.platform.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色实体类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
//@Builder
@SuperBuilder
//@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统角色实体类")
public class SysRole extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "系统内置角色标识: 0非系统内部角色,1系统内部角色")
    private Integer isBuiltin;

    @Schema(description = "角色编码,如ROLE_ADMIN")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "逻辑删除标记:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "状态:1正常,0停用")
    private Integer status;
}
