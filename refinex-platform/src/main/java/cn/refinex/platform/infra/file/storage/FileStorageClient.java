package cn.refinex.platform.infra.file.storage;

import cn.refinex.core.exception.FileStorageException;
import cn.refinex.platform.entity.SysStorageConfig;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * 抽象的文件存储客户端。
 * 负责与具体存储介质对接（S3/本地/数据库等），由工厂根据配置选择实现。
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface FileStorageClient {

    /**
     * 将数据流上传为对象。
     *
     * @param config      存储配置
     * @param objectKey   对象键（存储路径/Key）。若为 null 则由实现生成一个。
     * @param in          输入流
     * @param contentLen  内容长度（未知则传 -1）
     * @param contentType MIME 类型（可为 null）
     * @return 真实存储的对象键
     */
    String upload(SysStorageConfig config, String objectKey, InputStream in, long contentLen, String contentType) throws FileStorageException;

    /**
     * 下载对象数据流。
     *
     * @param config    存储配置
     * @param objectKey 存储对象键
     * @return 数据流
     */
    InputStream download(SysStorageConfig config, String objectKey) throws FileStorageException;

    /**
     * 删除对象。
     *
     * @param config    存储配置
     * @param objectKey 存储对象键
     */
    void delete(SysStorageConfig config, String objectKey) throws FileStorageException;

    /**
     * 生成可访问的 URI（若无对外域名则返回内部 URI，例如 s3:// 或 file://）。
     *
     * @param config    存储配置
     * @param objectKey 存储对象键
     * @return URI
     */
    URI toUri(SysStorageConfig config, String objectKey);

    // --------------- 分片上传（可选支持） ---------------

    /**
     * 初始化分片上传（返回 uploadId）。
     *
     * @param config      存储配置
     * @param objectKey   存储对象键
     * @param contentType MIME 类型
     * @return uploadId 上传 ID
     * @throws FileStorageException 抛出异常
     */
    default String initiateMultipartUpload(SysStorageConfig config, String objectKey, String contentType) throws FileStorageException {
        throw new UnsupportedOperationException("Multipart upload not supported");
    }

    /**
     * 上传一个分片，返回该分片的 ETag 或实现定义的标识。
     *
     * @param config     存储配置
     * @param objectKey  存储对象键
     * @param uploadId   上传 ID
     * @param partNumber 分片编号
     * @param in         输入流
     * @param partSize   分片大小
     * @return ETag 或标识
     * @throws FileStorageException 抛出异常
     */
    default String uploadPart(SysStorageConfig config, String objectKey, String uploadId, int partNumber, InputStream in, long partSize) throws FileStorageException {
        throw new UnsupportedOperationException("Multipart upload not supported");
    }

    /**
     * 完成分片上传。
     *
     * @param config    存储配置
     * @param objectKey 存储对象键
     * @param uploadId  上传 ID
     * @param etags     ETag 列表
     * @throws FileStorageException 抛出异常
     */
    default void completeMultipartUpload(SysStorageConfig config, String objectKey, String uploadId, List<String> etags) throws FileStorageException {
        throw new UnsupportedOperationException("Multipart upload not supported");
    }

    /**
     * 终止分片上传。
     *
     * @param config    存储配置
     * @param objectKey 存储对象键
     * @param uploadId  上传 ID
     * @throws FileStorageException 抛出异常
     */
    default void abortMultipartUpload(SysStorageConfig config, String objectKey, String uploadId) throws FileStorageException {
        throw new UnsupportedOperationException("Multipart upload not supported");
    }
}
