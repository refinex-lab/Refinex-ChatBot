package cn.refinex.platform.infra.file.dto;

/**
 * 上传选项
 */
public record UploadOptions(
        String storageCode,
        String bizType,
        String bizId,
        String title,
        boolean imageCompress,
        Integer imageMaxWidth,
        Float imageQuality
) {}
