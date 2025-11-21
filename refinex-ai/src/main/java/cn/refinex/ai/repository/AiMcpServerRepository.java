package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiMcpServer;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP Server 仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiMcpServerRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增 MCP Server
     *
     * @param server  MCP Server
     * @param jdbcTx 事务管理器
     * @return 主键
     */
    public long insert(AiMcpServer server, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_mcp_server (
                  server_code, server_name, transport_type, entry_command, endpoint_url,
                  manifest_url, auth_type, auth_secret_cipher, auth_secret_index,
                  tools_filter, status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :serverCode, :serverName, :transportType, :entryCommand, :endpointUrl,
                  :manifestUrl, :authType, :authSecretCipher, :authSecretIndex,
                  :toolsFilter, :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(server, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新 MCP Server
     *
     * @param server  MCP Server
     * @param jdbcTx 事务管理器
     */
    public void update(AiMcpServer server, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_mcp_server
                SET server_code       = :serverCode,
                    server_name       = :serverName,
                    transport_type    = :transportType,
                    entry_command     = :entryCommand,
                    endpoint_url      = :endpointUrl,
                    manifest_url      = :manifestUrl,
                    auth_type         = :authType,
                    auth_secret_cipher = :authSecretCipher,
                    auth_secret_index  = :authSecretIndex,
                    tools_filter      = :toolsFilter,
                    status            = :status,
                    update_by         = :updateBy,
                    update_time       = :updateTime,
                    remark            = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(server, false, false));
    }

    /**
     * 根据 ID 查询 MCP Server
     *
     * @param id     主键
     * @param userId 用户 ID
     * @return MCP Server
     */
    public Optional<AiMcpServer> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_mcp_server
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiMcpServer.class));
    }

    /**
     * 根据 Server Code 查询 MCP Server
     *
     * @param code   Server Code
     * @param userId 用户 ID
     * @return MCP Server
     */
    public Optional<AiMcpServer> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_mcp_server
                WHERE server_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiMcpServer.class));
    }

    /**
     * 逻辑删除 MCP Server
     *
     * @param id      主键
     * @param operator 操作人
     * @param jdbcTx 事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_mcp_server
                SET deleted = 1,
                    delete_by = :operator,
                    delete_time = NOW(),
                    update_by = :operator,
                    update_time = NOW()
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, Map.of("id", id, "operator", operator));
    }

    /**
     * 分页查询 MCP Server
     *
     * @param transportType 传输类型
     * @param status        状态
     * @param keyword       搜索关键词
     * @param query         分页查询参数
     * @param userId        用户 ID
     * @return MCP Server 分页列表
     */
    public PageResponse<AiMcpServer> page(String transportType, Integer status, String keyword, PageQuery query, Long userId) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_mcp_server
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(transportType)) {
            sql.append(" AND transport_type = :transportType");
            params.put("transportType", transportType);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (server_code LIKE :keyword OR server_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, AiMcpServer.class);
    }
}
