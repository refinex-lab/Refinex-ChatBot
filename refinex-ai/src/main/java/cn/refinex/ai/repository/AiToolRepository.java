package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiTool;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.domain.PageQuery;
import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 工具仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiToolRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增 Tool
     *
     * @param tool  Tool
     * @param jdbcTx 事务管理器
     * @return 主键
     */
    public long insert(AiTool tool, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_tool (
                  tool_code, tool_name, tool_type, impl_bean, endpoint,
                  timeout_ms, input_schema, output_schema, mcp_server_id,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :toolCode, :toolName, :toolType, :implBean, :endpoint,
                  :timeoutMs, :inputSchema, :outputSchema, :mcpServerId,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(tool, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新 Tool
     *
     * @param tool  Tool
     * @param jdbcTx 事务管理器
     */
    public void update(AiTool tool, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_tool
                SET tool_code     = :toolCode,
                    tool_name     = :toolName,
                    tool_type     = :toolType,
                    impl_bean     = :implBean,
                    endpoint      = :endpoint,
                    timeout_ms    = :timeoutMs,
                    input_schema  = :inputSchema,
                    output_schema = :outputSchema,
                    mcp_server_id = :mcpServerId,
                    status        = :status,
                    update_by     = :updateBy,
                    update_time   = :updateTime,
                    remark        = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(tool, false, false));
    }

    /**
     * 根据主键查询 Tool
     *
     * @param id     主键
     * @param userId 用户 ID
     * @return Tool
     */
    public Optional<AiTool> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_tool
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiTool.class));
    }

    /**
     * 根据 Code 查询 Tool
     *
     * @param code   Code
     * @param userId 用户 ID
     * @return Tool
     */
    public Optional<AiTool> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_tool
                WHERE tool_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiTool.class));
    }

    /**
     * 逻辑删除 Tool
     *
     * @param id      主键
     * @param operator 操作人
     * @param jdbcTx  事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_tool
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
     * 分页查询 Tool
     *
     * @param toolType 类型
     * @param mcpServerId MCP 服务器 ID
     * @param status     状态
     * @param keyword    搜索关键词
     * @param query      分页参数
     * @param userId     用户 ID
     * @return Tool 列表
     */
    public PageResponse<AiTool> page(String toolType, Long mcpServerId, Integer status, String keyword, PageQuery query, Long userId) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_tool
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(toolType)) {
            sql.append(" AND tool_type = :toolType");
            params.put("toolType", toolType);
        }
        if (mcpServerId != null) {
            sql.append(" AND mcp_server_id = :mcpServerId");
            params.put("mcpServerId", mcpServerId);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (tool_code LIKE :keyword OR tool_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, AiTool.class);
    }

    /**
     * 根据主键列表查询 Tool
     *
     * @param ids     主键列表
     * @param userId 用户 ID
     * @return Tool 列表
     */
    public List<AiTool> findByIds(List<Long> ids, Long userId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT *
                FROM ai_tool
                WHERE id IN (:ids)
                  AND create_by = :userId
                  AND deleted = 0
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("userId", userId);

        return jdbcManager.queryList(sql, params, AiTool.class);
    }
}
