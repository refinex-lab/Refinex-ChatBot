package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiPrompt;
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
 * AI 提示词仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiPromptRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增提示词
     *
     * @param prompt 提示词
     * @param jdbcTx 事务管理器
     * @return 主键ID
     */
    public long insert(AiPrompt prompt, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_prompt (
                  prompt_code, prompt_name, category, description, template_format,
                  role, template, variables, examples, hash_sha256, input_schema,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :promptCode, :promptName, :category, :description, :templateFormat,
                  :role, :template, :variables, :examples, :hashSha256, :inputSchema,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(prompt, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新提示词
     *
     * @param prompt 提示词
     * @param jdbcTx 事务管理器
     */
    public void update(AiPrompt prompt, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_prompt
                SET prompt_code    = :promptCode,
                    prompt_name    = :promptName,
                    category       = :category,
                    description    = :description,
                    template_format = :templateFormat,
                    role           = :role,
                    template       = :template,
                    variables      = :variables,
                    examples       = :examples,
                    hash_sha256    = :hashSha256,
                    input_schema   = :inputSchema,
                    status         = :status,
                    update_by      = :updateBy,
                    update_time    = :updateTime,
                    remark         = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        Map<String, Object> params = BeanUtils.beanToMap(prompt, false, false);
        jdbcTx.update(sql, params);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 提示词
     */
    public Optional<AiPrompt> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_prompt
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        Map<String, Object> params = Map.of("id", id, "userId", userId);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, AiPrompt.class));
    }

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 提示词
     */
    public Optional<AiPrompt> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_prompt
                WHERE prompt_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        Map<String, Object> params = Map.of("code", code, "userId", userId);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, AiPrompt.class));
    }

    /**
     * 逻辑删除
     *
     * @param id       主键
     * @param operator 操作人
     * @param jdbcTx   事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_prompt
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
     * 分页查询
     *
     * @param category       分类
     * @param status         状态
     * @param templateFormat 模板格式
     * @param keyword        关键字
     * @param pageQuery      分页参数
     * @return 分页结果
     */
    public PageResponse<AiPrompt> page(String category, Integer status, String templateFormat, String keyword, PageQuery pageQuery, Long userId) {
        if (pageQuery == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_prompt
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (StringUtils.hasText(category)) {
            sql.append(" AND category = :category");
            params.put("category", category);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(templateFormat)) {
            sql.append(" AND template_format = :templateFormat");
            params.put("templateFormat", templateFormat);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append("""
                    AND (prompt_code LIKE :keyword
                         OR prompt_name LIKE :keyword
                         OR description LIKE :keyword)
                    """);
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC");
        return jdbcManager.queryPage(sql.toString(), params, pageQuery, AiPrompt.class);
    }
}
