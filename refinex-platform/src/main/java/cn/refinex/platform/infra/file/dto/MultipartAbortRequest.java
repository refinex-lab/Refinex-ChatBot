package cn.refinex.platform.infra.file.dto;

/**
 * 分片终止请求
 */
public record MultipartAbortRequest(
        String storageCode,
        String objectKey,
        String uploadId
) {}
