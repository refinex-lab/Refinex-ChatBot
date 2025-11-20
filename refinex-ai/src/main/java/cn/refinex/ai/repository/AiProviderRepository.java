package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiProvider;
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
 * 模型供应商仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiProviderRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入模型供应商
     *
     * @param provider 模型供应商
     * @param jdbcTx 数据库事务
     * @return 主键
     */
    public long insert(AiProvider provider, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_provider (
                  provider_code, provider_name, provider_type, base_url,
                  api_key_cipher, api_key_index, rate_limit_qpm,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :providerCode, :providerName, :providerType, :baseUrl,
                  :apiKeyCipher, :apiKeyIndex, :rateLimitQpm,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(provider, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新模型供应商
     *
     * @param provider 模型供应商
     * @param jdbcTx 数据库事务
     */
    public void update(AiProvider provider, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_provider
                SET provider_code  = :providerCode,
                    provider_name  = :providerName,
                    provider_type  = :providerType,
                    base_url       = :baseUrl,
                    api_key_cipher = :apiKeyCipher,
                    api_key_index  = :apiKeyIndex,
                    rate_limit_qpm = :rateLimitQpm,
                    status         = :status,
                    update_by      = :updateBy,
                    update_time    = :updateTime,
                    remark         = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(provider, false, false));
    }

    /**
     * 根据ID查询模型供应商
     *
     * @param id 主键
     * @param userId 用户ID
     * @return 模型供应商
     */
    public Optional<AiProvider> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_provider
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiProvider.class));
    }

    /**
     * 根据供应商编码查询模型供应商
     *
     * @param code 供应商编码
     * @param userId 用户ID
     * @return 模型供应商
     */
    public Optional<AiProvider> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_provider
                WHERE provider_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiProvider.class));
    }

    /**
     * 逻辑删除模型供应商
     *
     * @param id 主键
     * @param operator 操作人
     * @param jdbcTx 数据库事务
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_provider
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
     * 分页查询模型供应商
     *
     * @param providerType 供应商类型
     * @param status 状态
     * @param keyword 关键词
     * @param pageQuery 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    public PageResponse<AiProvider> page(String providerType, Integer status, String keyword, PageQuery pageQuery, Long userId) {
        if (pageQuery == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_provider
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (StringUtils.hasText(providerType)) {
            sql.append(" AND provider_type = :providerType");
            params.put("providerType", providerType);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (provider_code LIKE :keyword OR provider_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC");
        return jdbcManager.queryPage(sql.toString(), params, pageQuery, AiProvider.class);
    }
}
