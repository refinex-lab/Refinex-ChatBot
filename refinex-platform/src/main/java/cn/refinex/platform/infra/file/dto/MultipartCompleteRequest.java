package cn.refinex.platform.infra.file.dto;

import java.util.List;

/**
 * 分片完成请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartCompleteRequest(
        /* 存储编码 */
        String storageCode,
        /* 对象Key */
        String objectKey,
        /* 上传ID */
        String uploadId,
        /* 分片信息 */
        List<String> etags,
        /* 文件名称 */
        String fileName,
        /* 文件媒体类型 */
        String mimeType,
        /* 业务类型 */
        String bizType,
        /* 业务ID */
        String bizId,
        /* 文件标题 */
        String title
) {}
