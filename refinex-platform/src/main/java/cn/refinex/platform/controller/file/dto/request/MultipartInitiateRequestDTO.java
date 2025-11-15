package cn.refinex.platform.controller.file.dto.request;

/**
 * 初始化分片上传请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartInitiateRequestDTO(
        /* 存储配置编码 */
        String storageCode,
        /* 文件名 */
        String fileName,
        /* 文件 MIME */
        String contentType
) {}

