package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 附件响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "附件响应")
public class ChatAttachmentResponseDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "URI")
    private String uri;

    @Schema(description = "存储提供方")
    private String storageProvider;

    @Schema(description = "媒体类型")
    private String mediaType;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "大小")
    private Long sizeBytes;

    @Schema(description = "宽度")
    private Integer width;

    @Schema(description = "高度")
    private Integer height;

    @Schema(description = "时长")
    private Long durationMs;

    @Schema(description = "转写文本")
    private String transcriptText;

    @Schema(description = "知识文档ID")
    private Long kbDocId;

    @Schema(description = "分片ID")
    private Long kbChunkId;

    @Schema(description = "外部向量ID")
    private String externalVectorId;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;
}
