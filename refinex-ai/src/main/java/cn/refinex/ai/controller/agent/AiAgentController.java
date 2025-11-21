package cn.refinex.ai.controller.agent;

import cn.refinex.ai.controller.agent.dto.request.AiAgentCreateRequestDTO;
import cn.refinex.ai.controller.agent.dto.request.AiAgentPageRequest;
import cn.refinex.ai.controller.agent.dto.request.AiAgentUpdateRequestDTO;
import cn.refinex.ai.controller.agent.dto.request.AiAgentUpdateStatusRequest;
import cn.refinex.ai.controller.agent.dto.response.AiAgentResponseDTO;
import cn.refinex.ai.service.AiAgentService;
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
 * Agent 管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/agents")
@Tag(name = "AI Agent 管理", description = "Agent CRUD 接口")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    @Operation(summary = "分页查询 Agent")
    @GetMapping
    public ApiResponse<PageResponse<AiAgentResponseDTO>> page(@Valid AiAgentPageRequest request) {
        return ApiResponse.success(aiAgentService.page(request));
    }

    @Operation(summary = "根据ID获取 Agent")
    @GetMapping("/{id}")
    public ApiResponse<AiAgentResponseDTO> get(@PathVariable("id") Long id) {
        return aiAgentService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "Agent 不存在"));
    }

    @Operation(summary = "创建 Agent")
    @PostMapping
    @RequestLog(title = "创建AI Agent", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiAgentCreateRequestDTO request) {
        aiAgentService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Agent")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "Agent ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新AI Agent", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiAgentUpdateRequestDTO request) {
        aiAgentService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Agent 状态")
    @PatchMapping("/{id}/status")
    @RequestLog(title = "更新AI Agent状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AiAgentUpdateStatusRequest request) {
        aiAgentService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除 Agent")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除AI Agent", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiAgentService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
