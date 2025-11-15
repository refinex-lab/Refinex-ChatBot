package cn.refinex.kb.entity;

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
 * 知识库目录
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识库目录")
public class KbCatalog extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "知识库ID(逻辑关联 kb_base.id)")
    private Long kbId;

    @Schema(description = "父目录ID(根为空)")
    private Long parentId;

    @Schema(description = "层级路径表达，如 /a/b")
    private String path;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "层级深度，从0开始")
    private Integer depth;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "目录下文档数量(冗余)")
    private Integer docCount;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

