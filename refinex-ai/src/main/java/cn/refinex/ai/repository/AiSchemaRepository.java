package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiSchema;
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
 * Schema 仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiSchemaRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增 Schema
     *
     * @param schema Schema
     * @param jdbcTx 事务管理器
     * @return 主键
     */
    public long insert(AiSchema schema, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_schema (
                  schema_code, schema_name, schema_type, schema_json,
                  version, strict_mode, status,
                  create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :schemaCode, :schemaName, :schemaType, :schemaJson,
                  :version, :strictMode, :status,
                  :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(schema, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新 Schema
     *
     * @param schema Schema
     * @param jdbcTx 事务管理器
     */
    public void update(AiSchema schema, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_schema
                SET schema_code  = :schemaCode,
                    schema_name  = :schemaName,
                    schema_type  = :schemaType,
                    schema_json  = :schemaJson,
                    version      = :version,
                    strict_mode  = :strictMode,
                    status       = :status,
                    update_by    = :updateBy,
                    update_time  = :updateTime,
                    remark       = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(schema, false, false));
    }

    /**
     * 根据主键查询 Schema
     *
     * @param id     主键
     * @param userId 用户 ID
     * @return Schema
     */
    public Optional<AiSchema> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_schema
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiSchema.class));
    }

    /**
     * 根据 Code 查询 Schema
     *
     * @param code   Code
     * @param userId 用户 ID
     * @return Schema
     */
    public Optional<AiSchema> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_schema
                WHERE schema_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiSchema.class));
    }

    /**
     * 逻辑删除 Schema
     *
     * @param id      主键
     * @param operator 操作人
     * @param jdbcTx  事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_schema
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
     * 分页查询 Schema
     *
     * @param schemaType 类型
     * @param status     状态
     * @param keyword    搜索关键词
     * @param query      分页参数
     * @param userId     用户 ID
     * @return Schema 列表
     */
    public PageResponse<AiSchema> page(String schemaType, Integer status, String keyword, PageQuery query, Long userId) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_schema
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(schemaType)) {
            sql.append(" AND schema_type = :schemaType");
            params.put("schemaType", schemaType);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (schema_code LIKE :keyword OR schema_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, AiSchema.class);
    }
}
