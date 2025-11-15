package cn.refinex.platform.infra.file.storage.impl;

import cn.refinex.core.exception.FileStorageException;
import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.infra.file.storage.FileStorageClient;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 本地磁盘存储
 *
 * @author Refinex
 * @since 1.0.0
 */
public class LocalFileStorageClient implements FileStorageClient {

    /**
     * 上传文件
     *
     * @param config       存储配置
     * @param objectKey    对象键
     * @param in           输入流
     * @param contentLen   文件长度
     * @param contentType  文件类型
     * @return 对象键
     * @throws FileStorageException 存储异常
     */
    @Override
    public String upload(SysStorageConfig config, String objectKey, InputStream in, long contentLen, String contentType) throws FileStorageException {
        try {
            String basePath = config.getBasePath() == null ? "uploads" : config.getBasePath();
            if (objectKey == null || objectKey.isBlank()) {
                LocalDate today = LocalDate.now();
                objectKey = "%d/%02d/%02d/%s".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), UUID.randomUUID());
            }

            Path target = Paths.get(basePath, objectKey);
            Files.createDirectories(target.getParent());
            Files.copy(in, target);

            return objectKey;
        } catch (Exception e) {
            throw new FileStorageException("本地存储上传失败", e);
        }
    }

    /**
     * 下载文件
     *
     * @param config       存储配置
     * @param objectKey    对象键
     * @return 输入流
     * @throws FileStorageException 存储异常
     */
    @Override
    public InputStream download(SysStorageConfig config, String objectKey) throws FileStorageException {
        try {
            String basePath = config.getBasePath() == null ? "uploads" : config.getBasePath();
            Path target = Paths.get(basePath, objectKey);
            return Files.newInputStream(target);
        } catch (Exception e) {
            throw new FileStorageException("本地存储下载失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param config       存储配置
     * @param objectKey    对象键
     * @throws FileStorageException 存储异常
     */
    @Override
    public void delete(SysStorageConfig config, String objectKey) throws FileStorageException {
        try {
            String basePath = config.getBasePath() == null ? "uploads" : config.getBasePath();
            Path target = Paths.get(basePath, objectKey);
            Files.deleteIfExists(target);
        } catch (Exception e) {
            throw new FileStorageException("本地存储删除失败", e);
        }
    }

    /**
     * 获取文件URI
     *
     * @param config       存储配置
     * @param objectKey    对象键
     * @return 文件URI
     */
    @Override
    public URI toUri(SysStorageConfig config, String objectKey) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            return URI.create(baseUrl.endsWith("/") ? baseUrl + objectKey : baseUrl + "/" + objectKey);
        }

        String basePath = config.getBasePath() == null ? "uploads" : config.getBasePath();
        Path target = Paths.get(basePath, objectKey);

        return target.toUri();
    }
}
