package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiAdvisor;
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
 * Advisor 仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiAdvisorRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 插入 Advisor
     *
     * @param advisor  Advisor
     * @param jdbcTx   事务管理器
     * @return 主键
     */
    public long insert(AiAdvisor advisor, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_advisor (
                  advisor_code, advisor_name, advisor_type, sort,
                  status, create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :advisorCode, :advisorName, :advisorType, :sort,
                  :status, :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(advisor, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新 Advisor
     *
     * @param advisor  Advisor
     * @param jdbcTx   事务管理器
     */
    public void update(AiAdvisor advisor, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_advisor
                SET advisor_code = :advisorCode,
                    advisor_name = :advisorName,
                    advisor_type = :advisorType,
                    sort         = :sort,
                    status       = :status,
                    update_by    = :updateBy,
                    update_time  = :updateTime,
                    remark       = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(advisor, false, false));
    }

    /**
     * 根据 ID 查询 Advisor
     *
     * @param id     主键
     * @param userId 用户 ID
     * @return Advisor
     */
    public Optional<AiAdvisor> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_advisor
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiAdvisor.class));
    }

    /**
     * 根据 Code 查询 Advisor
     *
     * @param code   编码
     * @param userId 用户 ID
     * @return Advisor
     */
    public Optional<AiAdvisor> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_advisor
                WHERE advisor_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiAdvisor.class));
    }

    /**
     * 逻辑删除 Advisor
     *
     * @param id       主键
     * @param operator 操作人
     * @param jdbcTx   事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_advisor
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
     * 分页查询 Advisor
     *
     * @param advisorType 类型
     * @param status      状态
     * @param keyword     搜索关键词
     * @param query       分页参数
     * @param userId      用户 ID
     * @return Advisor 列表
     */
    public PageResponse<AiAdvisor> page(String advisorType, Integer status, String keyword, PageQuery query, Long userId) {
        if (query == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM ai_advisor
                WHERE create_by = :userId
                  AND deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (StringUtils.hasText(advisorType)) {
            sql.append(" AND advisor_type = :advisorType");
            params.put("advisorType", advisorType);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (advisor_code LIKE :keyword OR advisor_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY sort ASC, update_time DESC");

        return jdbcManager.queryPage(sql.toString(), params, query, AiAdvisor.class);
    }

    /**
     * 根据 ID 列表查询 Advisor
     *
     * @param ids     主键列表
     * @param userId 用户 ID
     * @return Advisor 列表
     */
    public List<AiAdvisor> findByIds(List<Long> ids, Long userId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        String sql = """
                SELECT *
                FROM ai_advisor
                WHERE id IN (:ids)
                  AND create_by = :userId
                  AND deleted = 0
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("userId", userId);

        return jdbcManager.queryList(sql, params, AiAdvisor.class);
    }
}
