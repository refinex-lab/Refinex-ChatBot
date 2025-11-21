package cn.refinex.ai.service;

import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerCreateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerPageRequest;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerUpdateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.response.AiMcpServerResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * MCP Server 服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiMcpServerService {

    /**
     * 分页查询 MCP Server
     *
     * @param request 查询请求
     * @return MCP Server 列表
     */
    PageResponse<AiMcpServerResponseDTO> page(AiMcpServerPageRequest request);

    /**
     * 根据 ID 查询 MCP Server
     *
     * @param id 主键
     * @return MCP Server
     */
    Optional<AiMcpServerResponseDTO> findById(Long id);

    /**
     * 新增 MCP Server
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    void create(AiMcpServerCreateRequestDTO request, Long operatorId);

    /**
     * 更新 MCP Server
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    void update(Long id, AiMcpServerUpdateRequestDTO request, Long operatorId);

    /**
     * 更新 MCP Server 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除 MCP Server
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    void delete(Long id, Long operatorId);
}
