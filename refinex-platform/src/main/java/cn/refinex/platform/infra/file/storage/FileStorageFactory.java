package cn.refinex.platform.infra.file.storage;

import cn.refinex.core.service.CryptoService;
import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.enums.StorageType;
import cn.refinex.platform.infra.file.storage.impl.DbFileStorageClient;
import cn.refinex.platform.infra.file.storage.impl.LocalFileStorageClient;
import cn.refinex.platform.infra.file.storage.impl.S3FileStorageClient;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件存储客户端工厂。
 * 基于 storageType 复用客户端实例（线程安全）。
 *
 * @author Refinex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class FileStorageFactory {

    private final CryptoService cryptoService;

    /**
     * 缓存文件存储客户端
     */
    private final Map<String, FileStorageClient> cache = new ConcurrentHashMap<>();

    /**
     * 获取文件存储客户端
     *
     * @param config 存储配置
     * @return 文件存储客户端
     */
    public FileStorageClient getClient(SysStorageConfig config) {
        String type = config.getStorageType();
        return cache.computeIfAbsent(type, t -> {
            StorageType storageType = StorageType.fromCode(t);
            return switch (storageType) {
                case DB -> new DbFileStorageClient();
                case LOCAL -> new LocalFileStorageClient();
                case S3, MINIO, OSS -> new S3FileStorageClient(cryptoService);
                // 占位：可替换为各自实现
                case GCS, AZURE -> new S3FileStorageClient(cryptoService);
            };
        });
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void close() {
        // 关闭 S3 客户端缓存
        S3FileStorageClient.shutdown();
        cache.clear();
    }
}
