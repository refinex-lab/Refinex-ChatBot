package cn.refinex.core.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志系统全局配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "refinex.logging")
public class RefinexLoggingProperties {

    /**
     * 是否启用增强日志能力
     */
    private boolean enabled = true;

    /**
     * 请求头 - 用于链路检索的 DataSign
     */
    private String dataSignHeader = "DataSign";

    /**
     * 请求头 - 自定义 TraceId
     */
    private String traceIdHeader = "X-Trace-Id";

    /**
     * 日志根目录 (logback 使用)
     */
    private String logRoot = "logs";

    /**
     * 日志滚动策略配置
     */
    private RollingPolicyProperties rolling = new RollingPolicyProperties();

    /**
     * 请求日志配置
     */
    private RequestLogProperties requestLog = new RequestLogProperties();

    @Data
    public static class RollingPolicyProperties {
        /**
         * 单个日志文件最大体积
         */
        private String maxFileSize = "128MB";

        /**
         * 最大日志保留天数
         */
        private int maxHistory = 30;

        /**
         * 日志总容量上限
         */
        private String totalSizeCap = "5GB";
    }

    @Data
    public static class RequestLogProperties {

        /**
         * 是否开启 @RequestLog
         */
        private boolean enabled = true;

        /**
         * 默认是否持久化
         */
        private boolean persist = true;

        /**
         * 默认是否记录请求体
         */
        private boolean recordRequestBody = true;

        /**
         * 默认是否记录响应体
         */
        private boolean recordResponseBody = false;

        /**
         * 请求/响应体最大记录字符数
         */
        private int bodyMaxLength = 2048;

        /**
         * 需要忽略记录的路径 (Ant 表达式)
         */
        private List<String> ignorePaths = new ArrayList<>(List.of("/actuator/**"));

        /**
         * 需要脱敏的字段名称
         */
        private List<String> sensitiveFields = new ArrayList<>(List.of(
                "password", "oldPassword", "newPassword", "confirmPassword", "token", "mobile"
        ));

        /**
         * 持久化使用的表名
         */
        private String tableName = "sys_request_log";
    }
}
