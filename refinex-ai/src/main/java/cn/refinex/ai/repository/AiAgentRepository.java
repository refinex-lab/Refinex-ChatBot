package cn.refinex.ai.repository;

import cn.refinex.ai.entity.AiAgent;
import cn.refinex.ai.entity.AiAgentAdvisor;
import cn.refinex.ai.entity.AiAgentTool;
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
 * Agent 仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AiAgentRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 新增 Agent
     *
     * @param agent  Agent
     * @param jdbcTx 事务管理器
     * @return 主键
     */
    public long insert(AiAgent agent, JdbcTemplateManager jdbcTx) {
        String sql = """
                INSERT INTO ai_agent (
                  agent_code, agent_name, description, model_id, prompt_id, output_schema_id,
                  rag_kb_id, temperature, top_p, presence_penalty, frequency_penalty,
                  max_tokens, stop_sequences, tool_choice, status,
                  create_by, create_time, update_by, update_time,
                  deleted, delete_by, delete_time, remark
                ) VALUES (
                  :agentCode, :agentName, :description, :modelId, :promptId, :outputSchemaId,
                  :ragKbId, :temperature, :topP, :presencePenalty, :frequencyPenalty,
                  :maxTokens, :stopSequences, :toolChoice, :status,
                  :createBy, :createTime, :updateBy, :updateTime,
                  :deleted, :deleteBy, :deleteTime, :remark
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource(BeanUtils.beanToMap(agent, false, false));
        return jdbcTx.insertAndGetKey(sql, params);
    }

    /**
     * 更新 Agent
     *
     * @param agent  Agent
     * @param jdbcTx 事务管理器
     */
    public void update(AiAgent agent, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_agent
                SET agent_code       = :agentCode,
                    agent_name       = :agentName,
                    description      = :description,
                    model_id         = :modelId,
                    prompt_id        = :promptId,
                    output_schema_id = :outputSchemaId,
                    rag_kb_id        = :ragKbId,
                    temperature      = :temperature,
                    top_p            = :topP,
                    presence_penalty = :presencePenalty,
                    frequency_penalty = :frequencyPenalty,
                    max_tokens       = :maxTokens,
                    stop_sequences   = :stopSequences,
                    tool_choice      = :toolChoice,
                    status           = :status,
                    update_by        = :updateBy,
                    update_time      = :updateTime,
                    remark           = :remark
                WHERE id = :id
                  AND deleted = 0
                """;
        jdbcTx.update(sql, BeanUtils.beanToMap(agent, false, false));
    }

    /**
     * 根据ID查询 Agent
     *
     * @param id     主键
     * @param userId 用户 ID
     * @return Agent
     */
    public Optional<AiAgent> findById(Long id, Long userId) {
        String sql = """
                SELECT *
                FROM ai_agent
                WHERE id = :id
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("id", id, "userId", userId), AiAgent.class));
    }

    /**
     * 根据编码查询 Agent
     *
     * @param code   编码
     * @param userId 用户 ID
     * @return Agent
     */
    public Optional<AiAgent> findByCode(String code, Long userId) {
        String sql = """
                SELECT *
                FROM ai_agent
                WHERE agent_code = :code
                  AND create_by = :userId
                  AND deleted = 0
                """;
        return Optional.ofNullable(jdbcManager.queryObject(sql, Map.of("code", code, "userId", userId), AiAgent.class));
    }

    /**
     * 分页查询 Agent
     *
     * @param modelId    模型 ID
     * @param providerId 供应商 ID
     * @param status     状态
     * @param keyword    关键词
     * @param pageQuery  分页参数
     * @param userId     用户 ID
     * @return Agent 分页列表
     */
    public PageResponse<AiAgent> page(Long modelId, Long providerId, Integer status, String keyword, PageQuery pageQuery, Long userId) {
        if (pageQuery == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT a.*
                FROM ai_agent a
                LEFT JOIN ai_model m ON a.model_id = m.id
                WHERE a.create_by = :userId
                  AND a.deleted = 0
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        if (modelId != null) {
            sql.append(" AND a.model_id = :modelId");
            params.put("modelId", modelId);
        }
        if (providerId != null) {
            sql.append(" AND m.provider_id = :providerId");
            params.put("providerId", providerId);
        }
        if (status != null) {
            sql.append(" AND a.status = :status");
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (a.agent_code LIKE :keyword OR a.agent_name LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY a.update_time DESC, a.id DESC");

        return jdbcManager.queryPage(sql.toString(), params, pageQuery, AiAgent.class);
    }

    /**
     * 逻辑删除 Agent
     *
     * @param id       主键
     * @param operator 操作人
     * @param jdbcTx   事务管理器
     */
    public void logicalDelete(Long id, Long operator, JdbcTemplateManager jdbcTx) {
        String sql = """
                UPDATE ai_agent
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
     * 查询 Agent 工具列表
     *
     * @param agentId Agent ID
     * @return 工具 ID 列表
     */
    public List<Long> listToolIds(Long agentId) {
        String sql = """
                SELECT tool_id
                FROM ai_agent_tool
                WHERE agent_id = :agentId
                ORDER BY sort ASC, id ASC
                """;

        List<Map<String, Object>> list = jdbcManager.queryList(sql, Map.of("agentId", agentId));
        List<Long> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Object val = map.get("tool_id");
            if (val instanceof Number number) {
                result.add(number.longValue());
            }
        }
        return result;
    }

    /**
     * 查询 Agent Advisors
     *
     * @param agentId Agent ID
     * @return Advisor ID 列表
     */
    public List<Long> listAdvisorIds(Long agentId) {
        String sql = """
                SELECT advisor_id
                FROM ai_agent_advisor
                WHERE agent_id = :agentId
                ORDER BY sort ASC, id ASC
                """;

        List<Map<String, Object>> list = jdbcManager.queryList(sql, Map.of("agentId", agentId));
        List<Long> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Object val = map.get("advisor_id");
            if (val instanceof Number number) {
                result.add(number.longValue());
            }
        }
        return result;
    }

    /**
     * 替换 Agent 工具关联
     *
     * @param agentId Agent ID
     * @param tools   工具列表
     * @param jdbcTx  事务管理器
     */
    public void replaceAgentTools(Long agentId, List<AiAgentTool> tools, JdbcTemplateManager jdbcTx) {
        jdbcTx.update("DELETE FROM ai_agent_tool WHERE agent_id = :agentId", Map.of("agentId", agentId));
        if (CollectionUtils.isEmpty(tools)) {
            return;
        }

        String sql = """
                INSERT INTO ai_agent_tool (agent_id, tool_id, sort, create_by, create_time)
                VALUES (:agentId, :toolId, :sort, :createBy, :createTime)
                """;

        for (AiAgentTool tool : tools) {
            tool.setAgentId(agentId);
            Map<String, Object> params = new HashMap<>(BeanUtils.beanToMap(tool, false, false));
            params.put("agentId", agentId);
            jdbcTx.update(sql, params);
        }
    }

    /**
     * 替换 Agent Advisor 关联
     *
     * @param agentId Agent ID
     * @param advisors Advisor 列表
     * @param jdbcTx  事务管理器
     */
    public void replaceAgentAdvisors(Long agentId, List<AiAgentAdvisor> advisors, JdbcTemplateManager jdbcTx) {
        jdbcTx.update("DELETE FROM ai_agent_advisor WHERE agent_id = :agentId", Map.of("agentId", agentId));
        if (CollectionUtils.isEmpty(advisors)) {
            return;
        }

        String sql = """
                INSERT INTO ai_agent_advisor (agent_id, advisor_id, sort, create_by, create_time)
                VALUES (:agentId, :advisorId, :sort, :createBy, :createTime)
                """;

        for (AiAgentAdvisor advisor : advisors) {
            advisor.setAgentId(agentId);
            Map<String, Object> params = new HashMap<>(BeanUtils.beanToMap(advisor, false, false));
            params.put("agentId", agentId);
            jdbcTx.update(sql, params);
        }
    }
}
