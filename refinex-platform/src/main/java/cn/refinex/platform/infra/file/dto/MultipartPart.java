package cn.refinex.platform.infra.file.dto;

/**
 * 分片信息
 *
 * @author Refinex
 * @since 1.0.0
 */
public record MultipartPart(
        /* 分片编号 */
        int partNumber,
        /* ETag */
        String eTag
) {}
