package cn.refinex.ai.repository;

import cn.refinex.ai.entity.ChatMessage;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 聊天消息仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class ChatMessageRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增聊天消息
     *
     * @param message 聊天消息
     * @param jdbcTx  数据库事务
     * @return 新增的消息 ID
     */
    public long insert(ChatMessage message, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO chat_message (
                  session_id, parent_message_id, role, message_type,
                  content_text, content_format, content_json, attachments_count,
                  tool_calls, tool_results, provider_id, model_id,
                  finish_reason, input_tokens, output_tokens, latency_ms,
                  cost, currency, error_code, error_message, message_time,
                  status, create_by, create_time, update_by, update_time,
                  deleted, remark
                ) VALUES (
                  :sessionId, :parentMessageId, :role, :messageType,
                  :contentText, :contentFormat, :contentJson, :attachmentsCount,
                  :toolCalls, :toolResults, :providerId, :modelId,
                  :finishReason, :inputTokens, :outputTokens, :latencyMs,
                  :cost, :currency, :errorCode, :errorMessage, :messageTime,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(message, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 根据消息 ID 查询聊天消息
     *
     * @param id     消息 ID
     * @param userId 用户 ID
     * @return 聊天消息
     */
    public Optional<ChatMessage> findById(Long id, Long userId) {
        String sql = """
                SELECT cm.*
                FROM chat_message cm
                JOIN chat_session cs ON cm.session_id = cs.id
                WHERE cm.id = :id
                  AND cs.user_id = :userId
                  AND cm.deleted = 0
                  AND cs.deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), ChatMessage.class));
    }

    /**
     * 根据会话 ID 查询聊天消息分页
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param query     分页查询参数
     * @return 聊天消息分页
     */
    public PageResponse<ChatMessage> pageBySession(Long sessionId, Long userId, PageQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        String sql = """
                SELECT cm.*
                FROM chat_message cm
                JOIN chat_session cs ON cm.session_id = cs.id
                WHERE cm.session_id = :sessionId
                  AND cs.user_id = :userId
                  AND cm.deleted = 0
                  AND cs.deleted = 0
                ORDER BY cm.message_time ASC, cm.id ASC
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", sessionId);
        params.put("userId", userId);

        return jdbcManager.queryPage(sql, params, query, ChatMessage.class);
    }

    /**
     * 查询会话最近消息(按时间倒序)
     *
     * @param sessionId 会话ID
     * @param limit     消息数量
     * @return 消息列表
     */
    public List<ChatMessage> listRecent(Long sessionId, int limit) {
        String sql = """
                SELECT *
                FROM chat_message
                WHERE session_id = :sessionId
                  AND deleted = 0
                ORDER BY message_time DESC, id DESC
                LIMIT :size
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", sessionId);
        params.put("size", limit);

        List<ChatMessage> list = jdbcManager.queryList(sql, params, ChatMessage.class);
        // 按时间倒序排序
        Collections.reverse(list);
        return list;
    }
}
