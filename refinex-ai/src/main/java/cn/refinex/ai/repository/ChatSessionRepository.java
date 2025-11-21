package cn.refinex.ai.repository;

import cn.refinex.ai.entity.ChatSession;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 聊天会话仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class ChatSessionRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增聊天会话
     *
     * @param session 聊天会话
     * @param jdbcTx  数据库事务
     * @return 新增的会话 ID
     */
    public long insert(ChatSession session, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO chat_session (
                  session_code, user_id, agent_id, title, summary,
                  pinned, archived, message_count, token_count, last_message_time,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :sessionCode, :userId, :agentId, :title, :summary,
                  :pinned, :archived, :messageCount, :tokenCount, :lastMessageTime,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(session, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新聊天会话
     *
     * @param session 聊天会话
     * @param jdbcTx  数据库事务
     */
    public void update(ChatSession session, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE chat_session
                SET session_code      = :sessionCode,
                    agent_id          = :agentId,
                    title             = :title,
                    summary           = :summary,
                    pinned            = :pinned,
                    archived          = :archived,
                    message_count     = :messageCount,
                    token_count       = :tokenCount,
                    last_message_time = :lastMessageTime,
                    status            = :status,
                    update_by         = :updateBy,
                    update_time       = :updateTime,
                    remark            = :remark
                WHERE id = :id
                  AND user_id = :userId
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(session, false, false));
    }

    /**
     * 根据会话 ID 查询聊天会话
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     * @return 聊天会话
     */
    public Optional<ChatSession> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM chat_session
                WHERE id = :id
                  AND user_id = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), ChatSession.class));
    }

    /**
     * 根据会话代码查询聊天会话
     *
     * @param code   会话代码
     * @param userId 用户 ID
     * @return 聊天会话
     */
    public Optional<ChatSession> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM chat_session
                WHERE session_code = :code
                  AND user_id = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), ChatSession.class));
    }

    /**
     * 分页查询聊天会话
     *
     * @param userId   用户 ID
     * @param status   会话状态
     * @param pinned   是否置顶
     * @param archived 是否归档
     * @param keyword  搜索关键词
     * @param query    分页查询参数
     * @return 聊天会话分页
     */
    public PageResponse<ChatSession> page(Long userId, Integer status, Integer pinned, Integer archived, String keyword, PageQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM chat_session
                WHERE user_id = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (pinned != null) {
            sql.append(" AND pinned = :pinned");
            params.put("pinned", pinned);
        }
        if (archived != null) {
            sql.append(" AND archived = :archived");
            params.put("archived", archived);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (title LIKE :keyword OR summary LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY pinned DESC, (last_message_time IS NULL), last_message_time DESC, update_time DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, ChatSession.class);
    }

    /**
     * 逻辑删除聊天会话
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     * @param jdbcTx 数据库事务
     */
    public void logicalDelete(Long id, Long userId, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE chat_session
                SET deleted = 1,
                    delete_by = :userId,
                    delete_time = NOW(),
                    update_by = :userId,
                    update_time = NOW()
                WHERE id = :id
                  AND user_id = :userId
                  AND deleted = 0
                """;
        jdbcTx.update(sql, Map.of("id", id, "userId", userId));
    }

    /**
     * 增量更新聊天会话统计信息
     *
     * @param sessionId     会话 ID
     * @param userId        用户 ID
     * @param messageDelta  消息增量
     * @param tokenDelta    令牌增量
     * @param lastMessageTime 最后消息时间
     * @param summary       会话摘要
     * @param jdbcTx        数据库事务
     */
    public void incrementStats(Long sessionId, Long userId, int messageDelta,
                               Integer tokenDelta, LocalDateTime lastMessageTime,
                               String summary, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE chat_session
                SET message_count = message_count + :messageDelta,
                    token_count = token_count + :tokenDelta,
                    last_message_time = :lastMessageTime,
                    summary = COALESCE(:summary, summary),
                    update_time = NOW(),
                    update_by = :userId
                WHERE id = :sessionId
                  AND user_id = :userId
                  AND deleted = 0
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("messageDelta", messageDelta);
        params.put("tokenDelta", tokenDelta == null ? 0 : tokenDelta);
        params.put("lastMessageTime", lastMessageTime);
        params.put("summary", summary);
        params.put("sessionId", sessionId);
        params.put("userId", userId);

        jdbcTx.update(sql, params);
    }
}
