package cn.refinex.core.logging.handler;

import cn.refinex.core.logging.model.RequestLogEntry;

/**
 * 请求日志处理器
 *
 * @author Refinex
 * @since 1.0.0
 */
@FunctionalInterface
public interface RequestLogHandler {

    /**
     * 处理请求日志
     *
     * @param entry    日志实体
     * @param persist 是否持久化
     */
    void handle(RequestLogEntry entry, boolean persist);
}
