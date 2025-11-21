package cn.refinex.ai.service;

import cn.refinex.ai.controller.agent.dto.request.AiAgentCreateRequestDTO;
import cn.refinex.ai.controller.agent.dto.request.AiAgentPageRequest;
import cn.refinex.ai.controller.agent.dto.request.AiAgentUpdateRequestDTO;
import cn.refinex.ai.controller.agent.dto.response.AiAgentResponseDTO;
import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * Agent 服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiAgentService {

    /**
     * 分页查询 Agent
     *
     * @param request 分页请求
     * @return Agent 列表
     */
    PageResponse<AiAgentResponseDTO> page(AiAgentPageRequest request);

    /**
     * 根据主键查询 Agent
     *
     * @param id 主键
     * @return Agent
     */
    Optional<AiAgentResponseDTO> findById(Long id);

    /**
     * 新增 Agent
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    void create(AiAgentCreateRequestDTO request, Long operatorId);

    /**
     * 更新 Agent
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    void update(Long id, AiAgentUpdateRequestDTO request, Long operatorId);

    /**
     * 更新 Agent 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除 Agent
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    void delete(Long id, Long operatorId);

    /**
     * 构建运行时上下文
     *
     * @param agentId    Agent 主键
     * @param operatorId 操作人 ID
     * @return 运行时上下文
     */
    ChatModelContext buildRuntimeContext(Long agentId, Long operatorId);
}
