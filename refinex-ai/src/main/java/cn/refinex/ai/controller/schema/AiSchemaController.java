package cn.refinex.ai.controller.schema;

import cn.refinex.ai.controller.schema.dto.request.AiSchemaCreateRequestDTO;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaPageRequest;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaUpdateRequestDTO;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaUpdateStatusRequest;
import cn.refinex.ai.controller.schema.dto.response.AiSchemaResponseDTO;
import cn.refinex.ai.service.AiSchemaService;
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
 * Schema 管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/schemas")
@Tag(name = "AI Schema 管理", description = "Schema CRUD 接口")
public class AiSchemaController {

    private final AiSchemaService aiSchemaService;

    @Operation(summary = "分页查询 Schema")
    @GetMapping
    public ApiResponse<PageResponse<AiSchemaResponseDTO>> page(@Valid AiSchemaPageRequest request) {
        return ApiResponse.success(aiSchemaService.page(request));
    }

    @Operation(summary = "根据ID获取 Schema")
    @GetMapping("/{id}")
    public ApiResponse<AiSchemaResponseDTO> get(@PathVariable("id") Long id) {
        return aiSchemaService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "Schema 不存在"));
    }

    @Operation(summary = "创建 Schema")
    @PostMapping
    @RequestLog(title = "创建Schema", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiSchemaCreateRequestDTO request) {
        aiSchemaService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Schema")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "Schema ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新Schema", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiSchemaUpdateRequestDTO request) {
        aiSchemaService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新 Schema 状态")
    @PatchMapping("/{id}/status")
    @RequestLog(title = "更新Schema状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AiSchemaUpdateStatusRequest request) {
        aiSchemaService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除 Schema")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除Schema", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiSchemaService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
