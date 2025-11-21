package cn.refinex.ai.repository;

import cn.refinex.ai.entity.ChatAttachment;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天附件仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class ChatAttachmentRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 批量新增聊天附件
     *
     * @param attachments 聊天附件列表
     * @param jdbcTx      数据库事务
     */
    public void batchInsert(List<ChatAttachment> attachments, JdbcTemplateManager jdbcTx) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }

        String sql = """
                INSERT INTO chat_attachment (
                  message_id, file_id, file_name, uri, storage_provider,
                  media_type, mime_type, size_bytes, width, height,
                  duration_ms, transcript_text, kb_doc_id, kb_chunk_id,
                  external_vector_id, metadata, status,
                  create_by, create_time, update_by, update_time,
                  deleted, remark
                ) VALUES (
                  :messageId, :fileId, :fileName, :uri, :storageProvider,
                  :mediaType, :mimeType, :sizeBytes, :width, :height,
                  :durationMs, :transcriptText, :kbDocId, :kbChunkId,
                  :externalVectorId, :metadata, :status,
                  :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :remark
                )
                """;

        for (ChatAttachment attachment : attachments) {
            jdbcTx.update(sql, BeanUtils.beanToMap(attachment, false, false));
        }
    }

    /**
     * 根据消息 ID 列表查询聊天附件
     *
     * @param messageIds 消息 ID 列表
     * @return 聊天附件列表
     */
    public List<ChatAttachment> listByMessageIds(List<Long> messageIds) {
        if (CollectionUtils.isEmpty(messageIds)) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT *
                FROM chat_attachment
                WHERE message_id IN (:messageIds)
                  AND deleted = 0
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("messageIds", messageIds);

        return jdbcManager.queryList(sql, params, ChatAttachment.class);
    }

    /**
     * 根据消息 ID 列表分组查询聊天附件
     *
     * @param messageIds 消息 ID 列表
     * @return 消息 ID 到聊天附件列表的映射
     */
    public Map<Long, List<ChatAttachment>> groupByMessageIds(List<Long> messageIds) {
        return listByMessageIds(messageIds).stream()
                .collect(Collectors.groupingBy(ChatAttachment::getMessageId));
    }
}
