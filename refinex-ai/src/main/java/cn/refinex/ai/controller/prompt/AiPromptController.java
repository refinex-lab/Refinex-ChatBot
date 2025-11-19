package cn.refinex.ai.controller.prompt;

import cn.refinex.ai.controller.prompt.dto.request.AiPromptCreateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptPageRequest;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptUpdateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.response.AiPromptResponseDTO;
import cn.refinex.ai.service.AiPromptService;
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
 * AI 提示词管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/prompts")
@Tag(name = "AI 提示词管理", description = "Spring AI Prompt CRUD 接口")
public class AiPromptController {

    private final AiPromptService aiPromptService;

    @Operation(summary = "分页查询提示词")
    @GetMapping
    public ApiResponse<PageResponse<AiPromptResponseDTO>> page(@Valid AiPromptPageRequest request) {
        return ApiResponse.success(aiPromptService.page(request));
    }

    @Operation(summary = "根据ID获取提示词")
    @GetMapping("/{id}")
    @Parameter(name = "id", description = "提示词ID")
    public ApiResponse<AiPromptResponseDTO> get(@PathVariable("id") Long id) {
        return aiPromptService.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "提示词不存在"));
    }

    @Operation(summary = "根据编码获取提示词")
    @GetMapping("/code/{code}")
    @Parameter(name = "code", description = "提示词编码")
    public ApiResponse<AiPromptResponseDTO> getByCode(@PathVariable("code") String code) {
        return aiPromptService.findByCode(code)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "提示词不存在"));
    }

    @Operation(summary = "创建提示词")
    @PostMapping
    @Parameter(name = "request", description = "创建请求")
    @RequestLog(title = "创建AI提示词", type = RequestLogType.CREATE)
    public ApiResponse<Void> create(@Valid @RequestBody AiPromptCreateRequestDTO request) {
        aiPromptService.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新提示词")
    @PutMapping("/{id}")
    @Parameters(value = {
            @Parameter(name = "id", description = "提示词ID"),
            @Parameter(name = "request", description = "更新请求")
    })
    @RequestLog(title = "更新AI提示词", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody AiPromptUpdateRequestDTO request) {
        aiPromptService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除提示词")
    @DeleteMapping("/{id}")
    @Parameter(name = "id", description = "提示词ID")
    @RequestLog(title = "删除AI提示词", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        aiPromptService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
