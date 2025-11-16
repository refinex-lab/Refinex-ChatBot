package cn.refinex.platform.infra.file.dto;

import java.io.InputStream;

/**
 * 分片上传请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartUploadPartRequest(
        /* 存储编码 */
        String storageCode,
        /* 对象Key */
        String objectKey,
        /* 上传ID */
        String uploadId,
        /* 分片编号 */
        int partNumber,
        /* 分片数据 */
        InputStream input,
        /* 分片大小 */
        long size
) {}
