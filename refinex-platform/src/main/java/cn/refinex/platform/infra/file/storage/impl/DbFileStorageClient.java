package cn.refinex.platform.infra.file.storage.impl;

import cn.refinex.platform.entity.SysStorageConfig;
import cn.refinex.platform.infra.file.storage.FileStorageClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * 数据库存储（仅演示：真实读写由上层服务使用 SysFileDataRepository 完成）。
 * 该实现仅负责生成 URI 等，上传/下载由服务层直接操作表，因此这里的上传/下载返回/抛异常。
 *
 * @author Refinex
 * @since 1.0.0
 */
public class DbFileStorageClient implements FileStorageClient {

    /**
     * 上传文件
     *
     * @param config      存储配置
     * @param objectKey   对象Key
     * @param in          输入流
     * @param contentLen  内容长度
     * @param contentType 内容类型
     * @return 对象Key
     */
    @Override
    public String upload(SysStorageConfig config, String objectKey, InputStream in, long contentLen, String contentType) {
        // 对于 DB 存储，由服务层处理数据表操作，返回给定/生成的 objectKey 即可
        return objectKey != null ? objectKey : "db/" + System.nanoTime();
    }

    /**
     * 下载文件
     *
     * @param config    存储配置
     * @param objectKey 对象Key
     * @return 输入流
     */
    @Override
    public InputStream download(SysStorageConfig config, String objectKey) {
        // DB 下载走服务层仓储，这里不会被调用，返回空流防御
        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * 删除文件
     *
     * @param config    存储配置
     * @param objectKey 对象Key
     */
    @Override
    public void delete(SysStorageConfig config, String objectKey) {
        // DB 删除走服务层仓储，这里无操作
    }

    /**
     * 获取文件URI
     *
     * @param config    存储配置
     * @param objectKey 对象Key
     * @return 文件URI
     */
    @Override
    public URI toUri(SysStorageConfig config, String objectKey) {
        return URI.create("db://" + (objectKey == null ? "" : objectKey));
    }
}

