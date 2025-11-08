package cn.refinex.jdbc.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JDBC 模板配置属性
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "refinex.jdbc")
public class JdbcTemplateProperties {

    /**
     * 是否启用 SQL 日志（默认关闭）
     */
    private boolean enableSqlLog = false;

    /**
     * 慢查询阈值（毫秒），默认 1000ms
     */
    private long slowQueryThresholdMs = 1000;

    /**
     * 敏感关键字列表
     * <p>
     * YAML 配置示例:
     * <pre>{@code
     * refinex:
     *   jdbc:
     *     sensitive-keys:
     *       - password
     *       - pwd
     *       - secret
     *       - token
     *       - accesskey
     *       - apikey
     *       - privatekey
     * }</pre>
     */
    private List<String> sensitiveKeys = Arrays.asList(
            "password", "pwd", "secret", "token", "accesskey", "apikey", "privatekey"
    );

    /**
     * 敏感模式列表（包含这些字符串的键将被脱敏）
     * <p>
     * YAML 配置示例:
     * <pre>{@code
     * refinex:
     *   jdbc:
     *     sensitive-patterns:
     *       - password
     *       - pwd
     *       - secret
     *       - token
     *       - accesskey
     *       - apikey
     *       - privatekey
     * }</pre>
     */
    private List<String> sensitivePatterns = new ArrayList<>();

    /**
     * 脱敏掩码值
     */
    private String maskValue = "******";

    /**
     * 是否启用列名转小写（默认启用）
     */
    private boolean lowerCaseColumnNames = true;

    /**
     * 数据库类型（mysql, oracle, postgresql）
     */
    private String databaseType = "mysql";

    /**
     * 是否启用列名冲突检测（默认启用）
     */
    private boolean enableColumnConflictCheck = true;

    /**
     * 日志格式（text 或 json）
     */
    private String logFormat = "text";

}
