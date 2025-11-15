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

/**
 * 聊天消息附件（多模态资源）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "聊天消息附件")
public class ChatAttachment extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "消息ID(逻辑关联 chat_message.id)")
    private Long messageId;

    @Schema(description = "关联系统文件ID(逻辑关联 sys_file.id)")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "存储URI(OSS/本地/HTTP等)")
    private String uri;

    @Schema(description = "存储提供方: local/minio/oss/s3/http")
    private String storageProvider;

    @Schema(description = "媒体类型: IMAGE/AUDIO/VIDEO/DOCUMENT/OTHER")
    private String mediaType;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "大小(字节)")
    private Long sizeBytes;

    @Schema(description = "图片宽")
    private Integer width;

    @Schema(description = "图片高")
    private Integer height;

    @Schema(description = "音视频时长(ms)")
    private Long durationMs;

    @Schema(description = "转写文本(可选)")
    private String transcriptText;

    @Schema(description = "关联知识文档ID(逻辑关联 kb_document.id)")
    private Long kbDocId;

    @Schema(description = "关联分片ID(逻辑关联 kb_chunk_meta.id)")
    private Long kbChunkId;

    @Schema(description = "外部向量ID(向量库)")
    private String externalVectorId;

    @Schema(description = "扩展元数据(JSON)")
    private String metadata;

    @Schema(description = "状态:1正常,0禁用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

