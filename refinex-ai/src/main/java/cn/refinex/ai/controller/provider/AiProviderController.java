package cn.refinex.ai.controller.provider;

import cn.refinex.ai.controller.provider.dto.request.AiProviderCreateRequestDTO;
import cn.refinex.ai.controller.provider.dto.request.AiProviderPageRequest;
import cn.refinex.ai.controller.provider.dto.request.AiProviderUpdateRequestDTO;
import cn.refinex.ai.controller.provider.dto.request.AiProviderUpdateStatusRequest;
import cn.refinex.ai.controller.provider.dto.response.AiProviderResponseDTO;
import cn.refinex.ai.service.AiProviderService;
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
 * 模型供应商接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/providers")
@Tag(name = "AI 模型供应商管理", description = "模型供应商 CRUD 接口")
public class AiProviderController {

    private final AiProviderService aiProviderService;

    @Operation(summary = "分页查询模型供应商")
    @GetMapping
    public ApiResponse<PageResponse<AiProviderResponseDTO>> page(@Valid AiProviderPageRequest request) {
        return ApiResponse.success(aiProviderService.page(request));
    }

    @Operation(summary = "根据ID获取模型供应商")
    @GetMapping("/{id}")
    @Parameter(name = "id", description = "供应商ID")
    public ApiResponse<AiProviderResponseDTO> get(@PathVariable("id") Long id) {
        return aiProviderService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "供应商不存在"));
    }

    @Operation(summary = "根据编码获取模型供应商")
    @GetMapping("/code/{code}")
    @Parameter(name = "code", description = "供应商编码")
    public ApiResponse<AiProviderResponseDTO> getByCode(@PathVariable("code") String code) {
        return aiProviderService.findByCode(code)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "供应商不存在"));
    }

    @Operation(summary = "创建模型供应商")
    @PostMapping
    @RequestLog(title = "创建模型供应商", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiProviderCreateRequestDTO request) {
        aiProviderService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新模型供应商")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "供应商ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新模型供应商", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiProviderUpdateRequestDTO request) {
        aiProviderService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新模型供应商状态")
    @PatchMapping("/{id}/status")
    @Parameters(value = {
            @Parameter(name = "id", description = "供应商ID"),
            @Parameter(name = "request", description = "状态更新请求")
    })
    @RequestLog(title = "更新模型供应商状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id,
                                          @Valid @RequestBody AiProviderUpdateStatusRequest request) {
        aiProviderService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除模型供应商")
    @DeleteMapping("/{id}")
    @Parameter(name = "id", description = "供应商ID")
    @RequestLog(title = "删除模型供应商", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiProviderService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
