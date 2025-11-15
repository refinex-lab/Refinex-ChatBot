package cn.refinex.platform.infra.file.dto;

import java.io.InputStream;

/**
 * 分片上传请求
 */
public record MultipartUploadPartRequest(
        String storageCode,
        String objectKey,
        String uploadId,
        int partNumber,
        InputStream input,
        long size
) {}
