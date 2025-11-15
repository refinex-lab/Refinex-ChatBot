package cn.refinex.platform.infra.file.dto;

/**
 * 分片上传会话
 */
public record MultipartSession(
        String storageCode,
        String objectKey,
        String uploadId
) {}
