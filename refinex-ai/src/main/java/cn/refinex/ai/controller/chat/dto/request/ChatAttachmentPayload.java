package cn.refinex.ai.controller.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 消息附件请求载荷
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "消息附件请求")
public record ChatAttachmentPayload(

        @Schema(description = "文件ID")
        Long fileId,

        @Schema(description = "文件名")
        String fileName,

        @Schema(description = "URI")
        String uri,

        @Schema(description = "存储提供方")
        String storageProvider,

        @Schema(description = "媒体类型")
        String mediaType,

        @Schema(description = "MIME 类型")
        String mimeType,

        @Schema(description = "大小(字节)")
        Long sizeBytes,

        @Schema(description = "宽度")
        Integer width,

        @Schema(description = "高度")
        Integer height,

        @Schema(description = "时长(ms)")
        Long durationMs,

        @Schema(description = "转写文本")
        String transcriptText,

        @Schema(description = "知识文档ID")
        Long kbDocId,

        @Schema(description = "知识分片ID")
        Long kbChunkId,

        @Schema(description = "外部向量ID")
        String externalVectorId,

        @Schema(description = "元数据")
        Map<String, Object> metadata
) {
}
