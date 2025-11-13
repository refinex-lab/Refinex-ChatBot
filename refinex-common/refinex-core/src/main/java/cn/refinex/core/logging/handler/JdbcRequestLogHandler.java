package cn.refinex.core.logging.handler;

import cn.refinex.core.autoconfigure.properties.RefinexLoggingProperties;
import cn.refinex.core.logging.model.RequestLogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * JDBC 请求日志持久化
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public record JdbcRequestLogHandler(
        /* JDBC 模板 */
        NamedParameterJdbcTemplate jdbcTemplate,
        /* 日志配置 */
        RefinexLoggingProperties properties
) implements RequestLogHandler {

    /* SQL 插入语句模板 */
    private static final String INSERT_SQL = """
            INSERT INTO %s
            (
                service_name, title, biz_type, description, request_uri, http_method, client_ip,
                user_agent, data_sign, trace_id, http_status, success, user_id, username,
                controller, method_name, request_body, response_body, error_message, duration_ms, create_time)
            VALUES (
                :serviceName, :title, :bizType, :description, :requestUri, :httpMethod, :clientIp,
                :userAgent, :dataSign, :traceId, :httpStatus, :success, :userId, :username,
                :controller, :methodName, :requestBody, :responseBody, :errorMessage, :durationMs, :createTime)
            """;

    /**
     * 处理请求日志
     *
     * @param entry   日志实体
     * @param persist 是否持久化
     */
    @Override
    public void handle(RequestLogEntry entry, boolean persist) {
        if (!persist) {
            return;
        }

        String sql = INSERT_SQL.formatted(properties.getRequestLog().getTableName());
        ZonedDateTime createTime = Instant.ofEpochMilli(entry.timestamp()).atZone(ZoneId.systemDefault());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("serviceName", entry.serviceName())
                .addValue("title", entry.title())
                .addValue("bizType", entry.type() != null ? entry.type().name() : null)
                .addValue("description", entry.description())
                .addValue("requestUri", entry.requestUri())
                .addValue("httpMethod", entry.httpMethod())
                .addValue("clientIp", entry.clientIp())
                .addValue("userAgent", entry.userAgent())
                .addValue("dataSign", entry.dataSign())
                .addValue("traceId", entry.traceId())
                .addValue("httpStatus", entry.httpStatus())
                .addValue("success", entry.success() ? 1 : 0)
                .addValue("userId", entry.userId())
                .addValue("username", entry.username())
                .addValue("controller", entry.controller())
                .addValue("methodName", entry.methodName())
                .addValue("requestBody", entry.requestBody())
                .addValue("responseBody", entry.responseBody())
                .addValue("errorMessage", entry.errorMessage())
                .addValue("durationMs", entry.durationMs())
                .addValue("createTime", createTime.toLocalDateTime());

        try {
            jdbcTemplate.update(sql, params);
        } catch (DataAccessException ex) {
            log.error("Failed to persist request log: {}", ex.getMessage());
        }
    }
}
