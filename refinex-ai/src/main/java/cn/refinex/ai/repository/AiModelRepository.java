package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiModel;
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
 * 模型仓储类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiModelRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入模型
     *
     * @param model 模型
     * @param jdbcTx 数据库事务
     * @return 主键
     */
    public long insert(AiModel model, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_model (
                  provider_id, model_key, model_name, model_type,
                  api_variant, region, context_window_tokens, max_output_tokens,
                  price_input_per_1k, price_output_per_1k, currency,
                  support_tool_call, support_vision, support_audio_in, support_audio_out, support_structured_out,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :providerId, :modelKey, :modelName, :modelType,
                  :apiVariant, :region, :contextWindowTokens, :maxOutputTokens,
                  :priceInputPer1k, :priceOutputPer1k, :currency,
                  :supportToolCall, :supportVision, :supportAudioIn, :supportAudioOut, :supportStructuredOut,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(model, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新模型
     *
     * @param model 模型
     * @param jdbcTx 数据库事务
     */
    public void update(AiModel model, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_model
                SET provider_id            = :providerId,
                    model_key              = :modelKey,
                    model_name             = :modelName,
                    model_type             = :modelType,
                    api_variant            = :apiVariant,
                    region                 = :region,
                    context_window_tokens  = :contextWindowTokens,
                    max_output_tokens      = :maxOutputTokens,
                    price_input_per_1k     = :priceInputPer1k,
                    price_output_per_1k    = :priceOutputPer1k,
                    currency               = :currency,
                    support_tool_call      = :supportToolCall,
                    support_vision         = :supportVision,
                    support_audio_in       = :supportAudioIn,
                    support_audio_out      = :supportAudioOut,
                    support_structured_out = :supportStructuredOut,
                    status                 = :status,
                    update_by              = :updateBy,
                    update_time            = :updateTime,
                    remark                 = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(model, false, false));
    }

    /**
     * 根据ID查询模型
     *
     * @param id 主键
     * @param userId 用户ID
     * @return 模型
     */
    public Optional<AiModel> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_model
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiModel.class));
    }

    /**
     * 根据供应商ID和模型键查询模型
     *
     * @param providerId 供应商ID
     * @param modelKey 模型键
     * @param userId 用户ID
     * @return 模型
     */
    public Optional<AiModel> findByProviderAndKey(Long providerId, String modelKey, Long userId) {
        String sql = """
                SELECT *
                FROM ai_model
                WHERE provider_id = :providerId
                  AND model_key = :modelKey
                  AND create_by = :userId
                  AND deleted = 0
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("providerId", providerId);
        params.put("modelKey", modelKey);
        params.put("userId", userId);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, AiModel.class));
    }

    /**
     * 逻辑删除模型
     *
     * @param id 主键
     * @param operator 操作人
     * @param jdbcTx 数据库事务
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_model
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
     * 分页查询模型
     *
     * @param providerId 供应商ID
     * @param modelType 模型类型
     * @param apiVariant API变体
     * @param status 状态
     * @param keyword 关键词
     * @param pageQuery 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    public PageResponse<AiModel> page(Long providerId, String modelType, String apiVariant,
                                      Integer status, String keyword, PageQuery pageQuery, Long userId) {
        if (pageQuery == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_model
                WHERE create_by = :userId
                  AND deleted = 0
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (providerId != null) {
            sql.append(" AND provider_id = :providerId");
            params.put("providerId", providerId);
        }
        if (StringUtils.hasText(modelType)) {
            sql.append(" AND model_type = :modelType");
            params.put("modelType", modelType);
        }
        if (StringUtils.hasText(apiVariant)) {
            sql.append(" AND api_variant = :apiVariant");
            params.put("apiVariant", apiVariant);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (model_key LIKE :keyword OR model_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }

        sql.append(" ORDER BY update_time DESC, id DESC");
        return jdbcManager.queryPage(sql.toString(), params, pageQuery, AiModel.class);
    }
}
