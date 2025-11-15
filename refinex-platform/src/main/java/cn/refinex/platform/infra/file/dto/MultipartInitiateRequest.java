package cn.refinex.platform.infra.file.dto;

/**
 * 分片初始化请求
 */
public record MultipartInitiateRequest(
        String storageCode,
        String fileName,
        String contentType
) {}
