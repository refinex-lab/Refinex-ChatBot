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
import java.time.LocalDateTime;

/**
 * 知识文档
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识文档")
public class KbDocument extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "知识库ID(逻辑关联 kb_base.id)")
    private Long kbId;

    @Schema(description = "目录ID(逻辑关联 kb_catalog.id)")
    private Long catalogId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "来源类型: UPLOAD/URL/REPO/S3/CONFLUENCE/GIT 等")
    private String sourceType;

    @Schema(description = "来源URI")
    private String sourceUri;

    @Schema(description = "语种: zh/en/ja 等")
    private String language;

    @Schema(description = "作者(可选)")
    private String author;

    @Schema(description = "文件名(上传场景)")
    private String fileName;

    @Schema(description = "扩展名")
    private String fileExt;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件大小")
    private Long sizeBytes;

    @Schema(description = "SHA-256 校验和")
    private String checksumSha256;

    @Schema(description = "页数(如PDF)")
    private Integer pageCount;

    @Schema(description = "最新版本号")
    private Integer latestVersion;

    @Schema(description = "已索引最新版本号")
    private Integer indexLatestVersion;

    @Schema(description = "外部文档ID(第三方/存储/向量库绑定)")
    private String externalDocId;

    @Schema(description = "向量集合名(如按库/目录分集合)")
    private String vectorStoreCollection;

    @Schema(description = "状态:1正常,0禁用")
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

