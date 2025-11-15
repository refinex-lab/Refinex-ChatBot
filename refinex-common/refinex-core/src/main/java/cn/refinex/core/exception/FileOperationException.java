package cn.refinex.core.exception;

import cn.refinex.core.api.ApiStatus;

/**
 * 文件操作异常（读/写/压缩/哈希等）
 *
 * @author Refinex
 * @since 1.0.0
 */
public class FileOperationException extends BaseException {

    /**
     * 构造文件操作异常
     *
     * @param message 异常信息
     */
    public FileOperationException(String message) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 构造文件操作异常
     *
     * @param message 异常信息
     * @param cause   异常 cause
     */
    public FileOperationException(String message, Throwable cause) {
        super(ApiStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
