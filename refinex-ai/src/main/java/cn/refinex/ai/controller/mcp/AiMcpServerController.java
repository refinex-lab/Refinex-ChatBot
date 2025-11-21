package cn.refinex.ai.controller.mcp;

import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerCreateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerPageRequest;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerUpdateRequestDTO;
import cn.refinex.ai.controller.mcp.dto.request.AiMcpServerUpdateStatusRequest;
import cn.refinex.ai.controller.mcp.dto.response.AiMcpServerResponseDTO;
import cn.refinex.ai.service.AiMcpServerService;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.logging.annotation.RequestLog;
import cn.refinex.core.logging.enums.RequestLogType;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * MCP Server 管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/mcp-servers")
@Tag(name = "MCP Server 管理", description = "MCP Server CRUD 接口")
public class AiMcpServerController {

    private final AiMcpServerService aiMcpServerService;

    @Operation(summary = "分页查询 MCP Server")
    @GetMapping
    public ApiResponse<PageResponse<AiMcpServerResponseDTO>> page(@Valid AiMcpServerPageRequest request) {
        return ApiResponse.success(aiMcpServerService.page(request));
    }

    @Operation(summary = "根据ID获取 MCP Server")
    @GetMapping("/{id}")
    public ApiResponse<AiMcpServerResponseDTO> get(@PathVariable("id") Long id) {
        return aiMcpServerService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "MCP Server 不存在"));
    }

    @Operation(summary = "创建 MCP Server")
    @PostMapping
    @RequestLog(title = "创建MCP Server", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiMcpServerCreateRequestDTO request) {
        aiMcpServerService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 MCP Server")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "MCP Server ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新MCP Server", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiMcpServerUpdateRequestDTO request) {
        aiMcpServerService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 MCP Server 状态")
    @PatchMapping("/{id}/status")
    @RequestLog(title = "更新MCP Server状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AiMcpServerUpdateStatusRequest request) {
        aiMcpServerService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除 MCP Server")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除MCP Server", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiMcpServerService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
