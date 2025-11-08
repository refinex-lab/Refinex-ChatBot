package cn.refinex.jdbc.callback;

import cn.refinex.core.exception.SystemException;
import cn.refinex.jdbc.core.JdbcTemplateManager;

/**
 * 事务回调接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * 在事务中执行的操作
     *
     * @param jdbcManager JdbcTemplateManager 对象
     * @return 操作结果
     * @throws SystemException 操作过程中可能抛出的异常
     */
    T doInTransaction(JdbcTemplateManager jdbcManager) throws SystemException;
}
