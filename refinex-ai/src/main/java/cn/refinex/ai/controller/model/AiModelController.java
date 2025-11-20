package cn.refinex.ai.controller.model;

import cn.refinex.ai.controller.model.dto.request.AiModelCreateRequestDTO;
import cn.refinex.ai.controller.model.dto.request.AiModelPageRequest;
import cn.refinex.ai.controller.model.dto.request.AiModelUpdateRequestDTO;
import cn.refinex.ai.controller.model.dto.request.AiModelUpdateStatusRequest;
import cn.refinex.ai.controller.model.dto.response.AiModelResponseDTO;
import cn.refinex.ai.service.AiModelService;
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
 * AI 模型接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/models")
@Tag(name = "AI 模型管理", description = "AI 模型 CRUD 接口")
public class AiModelController {

    private final AiModelService aiModelService;

    @Operation(summary = "分页查询模型")
    @GetMapping
    public ApiResponse<PageResponse<AiModelResponseDTO>> page(@Valid AiModelPageRequest request) {
        return ApiResponse.success(aiModelService.page(request));
    }

    @Operation(summary = "根据ID获取模型")
    @GetMapping("/{id}")
    @Parameter(name = "id", description = "模型ID")
    public ApiResponse<AiModelResponseDTO> get(@PathVariable("id") Long id) {
        return aiModelService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "模型不存在"));
    }

    @Operation(summary = "创建模型")
    @PostMapping
    @RequestLog(title = "创建AI模型", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiModelCreateRequestDTO request) {
        aiModelService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新模型")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "模型ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新AI模型", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiModelUpdateRequestDTO request) {
        aiModelService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新模型状态")
    @PatchMapping("/{id}/status")
    @Parameters(value = {
            @Parameter(name = "id", description = "模型ID"),
            @Parameter(name = "request", description = "状态更新请求")
    })
    @RequestLog(title = "更新AI模型状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id,
                                          @Valid @RequestBody AiModelUpdateStatusRequest request) {
        aiModelService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    @Parameter(name = "id", description = "模型ID")
    @RequestLog(title = "删除AI模型", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiModelService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
