package cn.refinex.ai.controller.tool;

import cn.refinex.ai.controller.tool.dto.request.AiToolCreateRequestDTO;
import cn.refinex.ai.controller.tool.dto.request.AiToolPageRequest;
import cn.refinex.ai.controller.tool.dto.request.AiToolUpdateRequestDTO;
import cn.refinex.ai.controller.tool.dto.request.AiToolUpdateStatusRequest;
import cn.refinex.ai.controller.tool.dto.response.AiToolResponseDTO;
import cn.refinex.ai.service.AiToolService;
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
 * 工具管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/tools")
@Tag(name = "AI 工具管理", description = "工具 CRUD 接口")
public class AiToolController {

    private final AiToolService aiToolService;

    @Operation(summary = "分页查询工具")
    @GetMapping
    public ApiResponse<PageResponse<AiToolResponseDTO>> page(@Valid AiToolPageRequest request) {
        return ApiResponse.success(aiToolService.page(request));
    }

    @Operation(summary = "根据ID获取工具")
    @GetMapping("/{id}")
    public ApiResponse<AiToolResponseDTO> get(@PathVariable("id") Long id) {
        return aiToolService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "工具不存在"));
    }

    @Operation(summary = "创建工具")
    @PostMapping
    @RequestLog(title = "创建AI工具", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiToolCreateRequestDTO request) {
        aiToolService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新工具")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "工具ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新AI工具", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiToolUpdateRequestDTO request) {
        aiToolService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新工具状态")
    @PatchMapping("/{id}/status")
    @RequestLog(title = "更新AI工具状态", type = RequestLogType.UPDATE)
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AiToolUpdateStatusRequest request) {
        aiToolService.updateStatus(id, request.status(), LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除工具")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除AI工具", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiToolService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
