package cn.refinex.ai.controller.chat;

import cn.refinex.ai.controller.chat.dto.request.AiUsageLogPageRequest;
import cn.refinex.ai.controller.chat.dto.response.AiUsageLogResponseDTO;
import cn.refinex.ai.service.AiUsageLogService;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.PageResponse;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用日志接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/chat/usage-logs")
@Tag(name = "AI Usage Log", description = "AI 使用日志查询")
public class AiUsageLogController {

    private final AiUsageLogService aiUsageLogService;

    @Operation(summary = "分页查询使用日志")
    @GetMapping
    public ApiResponse<PageResponse<AiUsageLogResponseDTO>> page(@Valid AiUsageLogPageRequest request) {
        return ApiResponse.success(aiUsageLogService.page(request, LoginHelper.getUserId()));
    }
}
