package cn.refinex.platform.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单实体类
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
@Schema(description = "系统菜单实体类")
public class SysMenu extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父级菜单ID，根节点为0")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单编码")
    private String menuCode;

    @Schema(description = "菜单类型: DIR/MENU/BUTTON")
    private String menuType;

    @Schema(description = "前端路由路径")
    private String routePath;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "是否显示:1显示,0隐藏")
    private Integer visible;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "状态:1正常,0禁用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;
}
