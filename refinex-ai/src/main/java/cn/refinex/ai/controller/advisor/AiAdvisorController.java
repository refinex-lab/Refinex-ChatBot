package cn.refinex.ai.controller.advisor;

import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorCreateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorPageRequest;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorUpdateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorUpdateStatusRequest;
import cn.refinex.ai.controller.advisor.dto.response.AiAdvisorResponseDTO;
import cn.refinex.ai.service.AiAdvisorService;
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
 * Advisor 管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/advisors")
@Tag(name = "AI Advisor 管理", description = "Advisor CRUD 接口")
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;

    @Operation(summary = "分页查询 Advisor")
    @GetMapping
    public ApiResponse<PageResponse<AiAdvisorResponseDTO>> page(@Valid AiAdvisorPageRequest request) {
        return ApiResponse.success(aiAdvisorService.page(request));
    }

    @Operation(summary = "根据ID获取 Advisor")
    @GetMapping("/{id}")
    public ApiResponse<AiAdvisorResponseDTO> get(@PathVariable("id") Long id) {
        return aiAdvisorService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "Advisor 不存在"));
    }

    @Operation(summary = "创建 Advisor")
    @PostMapping
    @RequestLog(title = "创建Advisor", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiAdvisorCreateRequestDTO request) {
        aiAdvisorService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Advisor")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "Advisor ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新Advisor", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiAdvisorUpdateRequestDTO request) {
        aiAdvisorService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Advisor 状态")
    @PatchMapping("/{id}/status")
    @RequestLog(title = "更新Advisor状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AiAdvisorUpdateStatusRequest request) {
        aiAdvisorService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除 Advisor")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除Advisor", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiAdvisorService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
