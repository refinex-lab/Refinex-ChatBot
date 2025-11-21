package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiUsageLog;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用日志仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiUsageLogRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增使用日志
     *
     * @param log    使用日志
     * @param jdbcTx 数据库事务
     */
    public void insert(AiUsageLog log, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_usage_log (
                  request_id, user_id, session_id, provider_id, model_id,
                  model_key, operation, input_tokens, output_tokens,
                  cost, currency, success, http_status, latency_ms, create_time
                ) VALUES (
                  :requestId, :userId, :sessionId, :providerId, :modelId,
                  :modelKey, :operation, :inputTokens, :outputTokens,
                  :cost, :currency, :success, :httpStatus, :latencyMs, :createTime
                )
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(log, false, false));
    }

    /**
     * 分页查询使用日志
     *
     * @param userId    用户 ID
     * @param operation 操作类型
     * @param success   是否成功
     * @param query     分页查询参数
     * @return 使用日志列表
     */
    public PageResponse<AiUsageLog> page(Long userId, String operation, Boolean success, PageQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_usage_log
                WHERE (:userId IS NULL OR user_id = :userId)
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(operation)) {
            sql.append(" AND operation = :operation");
            params.put("operation", operation);
        }
        if (success != null) {
            sql.append(" AND success = :success");
            params.put("success", success ? 1 : 0);
        }
        sql.append(" ORDER BY create_time DESC, id DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, AiUsageLog.class);
    }
}
