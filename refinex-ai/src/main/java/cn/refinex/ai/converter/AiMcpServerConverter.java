package cn.refinex.ai.converter;

import cn.refinex.ai.controller.mcp.dto.response.AiMcpServerResponseDTO;
import cn.refinex.ai.entity.AiMcpServer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MCP Server 转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiMcpServerConverter {

    /**
     * 转换为 MCP Server 响应
     *
     * @param entity MCP Server 实体
     * @return MCP Server 响应
     */
    AiMcpServerResponseDTO toResponse(AiMcpServer entity);
}
