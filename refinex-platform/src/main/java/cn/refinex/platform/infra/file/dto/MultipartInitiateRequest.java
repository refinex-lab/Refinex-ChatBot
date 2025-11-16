package cn.refinex.platform.infra.file.dto;

/**
 * 分片初始化请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartInitiateRequest(
        /* 存储编码 */
        String storageCode,
        /* 文件名 */
        String fileName,
        /* 文件类型 */
        String contentType
) {}
