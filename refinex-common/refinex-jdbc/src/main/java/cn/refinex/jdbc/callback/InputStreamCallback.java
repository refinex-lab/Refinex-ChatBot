package cn.refinex.jdbc.callback;

import cn.refinex.core.exception.SystemException;

import java.io.InputStream;

/**
 * InputStream 回调接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@FunctionalInterface
public interface InputStreamCallback<T> {

    /**
     * 处理 InputStream 并返回结果
     *
     * @param inputStream InputStream 对象
     * @return 处理结果
     * @throws SystemException 处理过程中可能抛出的异常
     */
    T process(InputStream inputStream) throws SystemException;
}
