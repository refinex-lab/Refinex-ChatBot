package cn.refinex.platform.infra.file.storage.impl;

import cn.refinex.core.exception.FileStorageException;
import cn.refinex.core.service.CryptoService;
import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.infra.file.storage.FileStorageClient;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S3/兼容存储（包含 MinIO/OSS 兼容 S3 场景）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class S3FileStorageClient implements FileStorageClient {

    private final CryptoService cryptoService;
    private static final Map<String, S3Client> CLIENT_CACHE = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param cryptoService 加密服务
     */
    public S3FileStorageClient(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * 构建 S3 客户端
     *
     * @param cfg 存储配置
     * @return S3 客户端
     * @throws Exception 构建异常
     */
    private S3Client buildClient(SysStorageConfig cfg) throws Exception {
        String cacheKey = keyOf(cfg);
        S3Client cached = CLIENT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String accessKey = cfg.getAccessKeyCipher() != null ? cryptoService.decrypt(cfg.getAccessKeyCipher()) : null;
        String secretKey = cfg.getSecretKeyCipher() != null ? cryptoService.decrypt(cfg.getSecretKeyCipher()) : null;

        S3ClientBuilder builder = baseBuilder(cfg);
        maybeApplyCredentials(builder, accessKey, secretKey);
        S3Client client = builder.build();
        CLIENT_CACHE.put(cacheKey, client);
        return client;
    }

    /**
     * 构建 S3 构建器
     *
     * @param cfg 存储配置
     * @return S3 构建器
     */
    private static S3ClientBuilder baseBuilder(SysStorageConfig cfg) {
        S3ClientBuilder builder = S3Client.builder().httpClientBuilder(UrlConnectionHttpClient.builder());
        maybeApplyRegion(builder, cfg.getRegion());
        maybeApplyEndpoint(builder, cfg.getEndpoint());
        return builder;
    }

    /**
     * 尝试应用 region
     *
     * @param builder 构建器
     * @param region  region
     */
    private static void maybeApplyRegion(S3ClientBuilder builder, String region) {
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
    }

    /**
     * 尝试应用 endpoint
     *
     * @param builder  构建器
     * @param endpoint endpoint
     */
    private static void maybeApplyEndpoint(S3ClientBuilder builder, String endpoint) {
        if (endpoint != null && !endpoint.isBlank()) {
            try {
                builder.endpointOverride(new URI(endpoint));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid S3 endpoint: " + endpoint, e);
            }
        }
    }

    /**
     * 尝试应用 credentials
     *
     * @param builder   构建器
     * @param accessKey accessKey
     * @param secretKey secretKey
     */
    private static void maybeApplyCredentials(S3ClientBuilder builder, String accessKey, String secretKey) {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }
    }

    /**
     * 获取缓存 key
     *
     * @param cfg 存储配置
     * @return 缓存 key
     */
    private static String keyOf(SysStorageConfig cfg) {
        return String.join("|",
                nvl(cfg.getEndpoint()),
                nvl(cfg.getRegion()),
                nvl(cfg.getBucket()),
                nvl(cfg.getAccessKeyIndex()),
                nvl(cfg.getSecretKeyIndex()));
    }

    /**
     * 获取空值
     *
     * @param s 字符串
     * @return 空值
     */
    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * 上传文件
     *
     * @param config      存储配置
     * @param objectKey   存储对象Key
     * @param in          输入流
     * @param contentLen  内容长度
     * @param contentType 内容类型
     * @return 存储对象Key
     * @throws FileStorageException 上传异常
     */
    @Override
    public String upload(SysStorageConfig config, String objectKey, InputStream in, long contentLen, String contentType) throws FileStorageException {
        try {
            if (objectKey == null || objectKey.isBlank()) {
                LocalDate today = LocalDate.now();
                objectKey = "%d/%02d/%02d/%s".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), UUID.randomUUID());
            }

            try (S3Client s3 = buildClient(config)) {
                PutObjectRequest.Builder req = PutObjectRequest.builder()
                        .bucket(config.getBucket())
                        .key(objectKey);

                if (contentType != null && !contentType.isBlank()) {
                    req.contentType(contentType);
                }

                if (contentLen >= 0) {
                    s3.putObject(req.build(), RequestBody.fromInputStream(in, contentLen));
                } else {
                    s3.putObject(req.build(), RequestBody.fromInputStream(in, in.available()));
                }
            }
            return objectKey;
        } catch (Exception e) {
            throw new FileStorageException("S3 上传失败", e);
        }
    }

    /**
     * 下载文件
     *
     * @param config    存储配置
     * @param objectKey 存储对象Key
     * @return 输入流
     * @throws FileStorageException 下载异常
     */
    @Override
    @SuppressWarnings("java:S2095") // S3Client is cached and closed on shutdown/invalidation; do not close per call
    public InputStream download(SysStorageConfig config, String objectKey) throws FileStorageException {
        try {
            S3Client s3 = buildClient(config);
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .build();
            // 调用方负责关闭 InputStream；使用 ResponseInputStream
            return s3.getObject(req);
        } catch (Exception e) {
            throw new FileStorageException("S3 下载失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param config    存储配置
     * @param objectKey 存储对象Key
     * @throws FileStorageException 删除异常
     */
    @Override
    public void delete(SysStorageConfig config, String objectKey) throws FileStorageException {
        try (S3Client s3 = buildClient(config)) {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("S3 删除失败", e);
        }
    }

    /**
     * 获取文件URI
     *
     * @param config    存储配置
     * @param objectKey 存储对象Key
     * @return 文件URI
     */
    @Override
    public URI toUri(SysStorageConfig config, String objectKey) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            return URI.create(baseUrl.endsWith("/") ? baseUrl + objectKey : baseUrl + "/" + objectKey);
        }
        return URI.create("s3://" + config.getBucket() + "/" + objectKey);
    }

    /**
     * 初始化分片上传
     *
     * @param config      存储配置
     * @param objectKey   存储对象Key
     * @param contentType 内容类型
     * @return 存储对象Key
     * @throws FileStorageException 初始化异常
     */
    @Override
    public String initiateMultipartUpload(SysStorageConfig config, String objectKey, String contentType) throws FileStorageException {
        try {
            if (objectKey == null || objectKey.isBlank()) {
                LocalDate today = LocalDate.now();
                objectKey = "%d/%02d/%02d/%s".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), UUID.randomUUID());
            }

            try (S3Client s3 = buildClient(config)) {
                CreateMultipartUploadRequest.Builder req = CreateMultipartUploadRequest.builder()
                        .bucket(config.getBucket())
                        .key(objectKey);

                if (contentType != null && !contentType.isBlank()) {
                    req.contentType(contentType);
                }

                CreateMultipartUploadResponse res = s3.createMultipartUpload(req.build());
                // 返回复合标识（包含 objectKey）
                return objectKey + "|" + res.uploadId();
            }
        } catch (Exception e) {
            throw new FileStorageException("S3 初始化分片上传失败", e);
        }
    }

    /**
     * 上传分片
     *
     * @param config     存储配置
     * @param objectKey  存储对象Key
     * @param uploadId   上传ID
     * @param partNumber 分片编号
     * @param in         输入流
     * @param partSize   分片大小
     * @return 分片ETag
     * @throws FileStorageException 上传异常
     */
    @Override
    public String uploadPart(SysStorageConfig config, String objectKey, String uploadId, int partNumber, InputStream in, long partSize) throws FileStorageException {
        try (S3Client s3 = buildClient(config)) {
            UploadPartResponse res = s3.uploadPart(UploadPartRequest.builder()
                            .bucket(config.getBucket())
                            .key(objectKey)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .build(),
                    RequestBody.fromInputStream(in, partSize));
            return res.eTag();
        } catch (Exception e) {
            throw new FileStorageException("S3 上传分片失败", e);
        }
    }

    /**
     * 完成分片上传
     *
     * @param config    存储配置
     * @param objectKey 存储对象Key
     * @param uploadId  上传ID
     * @param etags     分片ETag列表
     * @throws FileStorageException 完成异常
     */
    @Override
    public void completeMultipartUpload(SysStorageConfig config, String objectKey, String uploadId, List<String> etags) throws FileStorageException {
        try (S3Client s3 = buildClient(config)) {
            List<CompletedPart> parts = new ArrayList<>();
            for (int i = 0; i < etags.size(); i++) {
                parts.add(CompletedPart.builder().partNumber(i + 1).eTag(etags.get(i)).build());
            }
            s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(m -> m.parts(parts))
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("S3 完成分片上传失败", e);
        }
    }

    /**
     * 放弃分片上传
     *
     * @param config    存储配置
     * @param objectKey 存储对象Key
     * @param uploadId  上传ID
     * @throws FileStorageException 放弃异常
     */
    @Override
    public void abortMultipartUpload(SysStorageConfig config, String objectKey, String uploadId) throws FileStorageException {
        try (S3Client s3 = buildClient(config)) {
            s3.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("S3 终止分片上传失败", e);
        }
    }

    /**
     * 关闭并清理所有缓存的 S3Client
     */
    public static void shutdown() {
        CLIENT_CACHE.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception ignored) {
                // 忽略
            }
        });
        CLIENT_CACHE.clear();
    }

    /**
     * 按配置定点失效缓存客户端
     */
    public static void invalidate(cn.refinex.platform.entity.SysStorageConfig cfg) {
        String cacheKey = keyOf(cfg);
        S3Client client = CLIENT_CACHE.remove(cacheKey);
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
                // 忽略
            }
        }
    }
}
