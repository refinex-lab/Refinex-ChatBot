package cn.refinex.platform.controller.file.dto.request;

import java.util.List;

/**
 * 完成分片上传请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartCompleteRequestDTO(
        /* 存储配置编码 */
        String storageCode,
        /* 对象存储对象键 */
        String objectKey,
        /* 上传任务ID */
        String uploadId,
        /* 分片信息 */
        List<String> etags,
        /* 文件名 */
        String fileName,
        /* MIME类型 */
        String mimeType,
        /* 业务类型 */
        String bizType,
        /* 业务ID */
        String bizId,
        /* 标题 */
        String title
) {}

