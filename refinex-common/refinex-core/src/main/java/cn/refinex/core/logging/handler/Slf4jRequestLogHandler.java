package cn.refinex.core.logging.handler;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.model.RequestLogEntry;
import cn.refinex.json.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 SLF4J 处理器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class Slf4jRequestLogHandler implements RequestLogHandler {

    private final JsonUtils jsonUtils;
    private final boolean enabled;

    /**
     * 构造函数，初始化是否启用日志记录
     *
     * @param properties 日志配置属性
     * @param jsonUtils  JSON 工具
     */
    public Slf4jRequestLogHandler(RefinexLoggingProperties properties, JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
        this.enabled = properties == null || properties.getRequestLog().isEnabled();
    }

    /**
     * 处理请求日志
     *
     * @param entry   日志实体
     * @param persist 是否持久化
     */
    @Override
    public void handle(RequestLogEntry entry, boolean persist) {
        if (!enabled) {
            return;
        }
        log.info("REQUEST_LOG persist={} {}", persist, jsonUtils.toJson(entry));
    }
}
