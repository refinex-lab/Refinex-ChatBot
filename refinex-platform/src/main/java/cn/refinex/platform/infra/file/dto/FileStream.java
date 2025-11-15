package cn.refinex.platform.infra.file.dto;

import java.io.InputStream;

/**
 * 文件流响应
 */
public record FileStream(
        InputStream stream,
        String fileName,
        String mimeType,
        long contentLength
) {}
