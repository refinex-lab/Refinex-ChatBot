package cn.refinex.platform.infra.file.dto;

/**
 * 分片终止请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartAbortRequest(
        /* 存储编码 */
        String storageCode,
        /* 对象Key */
        String objectKey,
        /* 上传ID */
        String uploadId
) {}
