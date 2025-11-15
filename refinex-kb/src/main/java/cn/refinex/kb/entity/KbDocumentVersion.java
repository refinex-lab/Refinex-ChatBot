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
 * 知识文档版本
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识文档版本")
public class KbDocumentVersion extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文档ID(逻辑关联 kb_document.id)")
    private Long docId;

    @Schema(description = "版本号(从1开始)")
    private Integer version;

    @Schema(description = "版本标题(可不同)")
    private String title;

    @Schema(description = "变更说明")
    private String changeLog;

    @Schema(description = "版本来源URI")
    private String sourceUri;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "扩展名")
    private String fileExt;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件大小")
    private Long sizeBytes;

    @Schema(description = "SHA-256 校验和")
    private String checksumSha256;

    @Schema(description = "解析状态: PENDING/PARSING/DONE/FAILED")
    private String parseStatus;

    @Schema(description = "索引状态: PENDING/INDEXING/DONE/FAILED")
    private String indexStatus;

    @Schema(description = "切片数量")
    private Integer chunkCount;

    @Schema(description = "向量条数(仅统计)")
    private Integer vectorCount;

    @Schema(description = "外部索引ID/任务ID")
    private String externalIndexId;

    @Schema(description = "状态:1有效,0无效")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

