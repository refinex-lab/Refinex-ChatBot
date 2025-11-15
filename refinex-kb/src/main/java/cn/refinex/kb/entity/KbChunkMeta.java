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
 * RAG 文档切片元数据
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "RAG 文档切片元数据")
public class KbChunkMeta extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文档版本ID(逻辑关联 kb_document_version.id)")
    private Long docVersionId;

    @Schema(description = "切片序号(从0或1开始)")
    private Integer chunkIndex;

    @Schema(description = "起始页(如有)")
    private Integer pageFrom;

    @Schema(description = "结束页(如有)")
    private Integer pageTo;

    @Schema(description = "起始偏移(字符)")
    private Integer startOffset;

    @Schema(description = "结束偏移(字符)")
    private Integer endOffset;

    @Schema(description = "估算 tokens")
    private Integer tokenCount;

    @Schema(description = "内容预览(不存全文)")
    private String contentPreview;

    @Schema(description = "外部向量ID")
    private String externalVectorId;

    @Schema(description = "外部分片/分区ID(可选)")
    private String externalShardId;

    @Schema(description = "元数据(JSON)，如标题/章节/标签")
    private String metadata;

    @Schema(description = "状态:1有效,0无效")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

