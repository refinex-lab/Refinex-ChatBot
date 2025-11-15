package cn.refinex.platform.infra.file.dto;

import java.util.List;

/**
 * 分片完成请求
 */
public record MultipartCompleteRequest(
        String storageCode,
        String objectKey,
        String uploadId,
        List<String> etags,
        String fileName,
        String mimeType,
        String bizType,
        String bizId,
        String title
) {}
