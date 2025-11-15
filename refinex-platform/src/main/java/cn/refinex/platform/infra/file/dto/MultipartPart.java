package cn.refinex.platform.infra.file.dto;

/**
 * 分片信息
 */
public record MultipartPart(
        int partNumber,
        String eTag
) {}
