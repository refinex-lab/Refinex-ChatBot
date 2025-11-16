package cn.refinex.platform.infra.file.dto;

/**
 * 上传选项
 *
 * @author Refinex
 * @since 1.0.0
 */
public record UploadOptions(
        /* 存储编码 */
        String storageCode,
        /* 业务类型 */
        String bizType,
        /* 业务ID */
        String bizId,
        /* 文件标题 */
        String title,
        /* 是否压缩图片 */
        boolean imageCompress,
        /* 图片最大宽度 */
        Integer imageMaxWidth,
        /* 图片压缩质量 */
        Float imageQuality
) {}
