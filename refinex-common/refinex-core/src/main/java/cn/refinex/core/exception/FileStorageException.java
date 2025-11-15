package cn.refinex.core.exception;

import cn.refinex.core.api.ApiStatus;

/**
 * 文件存储异常（S3/本地/DB 存储层）
 *
 * @author Refinex
 * @since 1.0.0
 */
public class FileStorageException extends BaseException {

    /**
     * 构造文件存储异常
     *
     * @param message 错误信息
     */
    public FileStorageException(String message) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 构造文件存储异常
     *
     * @param message 错误信息
     * @param cause   异常 cause
     */
    public FileStorageException(String message, Throwable cause) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
