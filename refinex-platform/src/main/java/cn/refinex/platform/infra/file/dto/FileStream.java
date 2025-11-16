package cn.refinex.platform.infra.file.dto;

import java.io.InputStream;

/**
 * 文件流响应
 *
 * @author Refinex
 * @since 1.0.0
 */
public record FileStream(
        /* 文件流 */
        InputStream stream,
        /* 文件名 */
        String fileName,
        /* 文件类型 */
        String mimeType,
        /* 文件大小 */
        long contentLength
) {}
