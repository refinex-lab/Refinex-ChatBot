package cn.refinex.platform.controller.file.dto.request;

/**
 * 终止分片上传请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartAbortRequestDTO(
        /* 存储配置编码 */
        String storageCode,
        /* 对象存储对象键 */
        String objectKey,
        /* 上传任务ID */
        String uploadId
) {}

