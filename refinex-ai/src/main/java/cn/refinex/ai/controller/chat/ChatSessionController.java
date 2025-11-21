package cn.refinex.ai.controller.chat;

import cn.refinex.ai.controller.chat.dto.request.ChatSessionCreateRequestDTO;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionPageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionUpdateRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatSessionResponseDTO;
import cn.refinex.ai.service.ChatSessionService;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.logging.annotation.RequestLog;
import cn.refinex.core.logging.enums.RequestLogType;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会话接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/chat/sessions")
@Tag(name = "Chat Session", description = "聊天会话管理")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @Operation(summary = "分页查询会话")
    @GetMapping
    public ApiResponse<PageResponse<ChatSessionResponseDTO>> page(@Valid ChatSessionPageRequest request) {
        return ApiResponse.success(chatSessionService.page(request, LoginHelper.getUserId()));
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/{id}")
    @Parameter(name = "id", description = "会话ID")
    public ApiResponse<ChatSessionResponseDTO> get(@PathVariable("id") Long id) {
        return chatSessionService.findById(id, LoginHelper.getUserId())
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "会话不存在"));
    }

    @Operation(summary = "创建会话")
    @PostMapping
    @RequestLog(title = "创建会话", type = RequestLogType.CREATE)
    public ApiResponse<ChatSessionResponseDTO> create(@Valid @RequestBody ChatSessionCreateRequestDTO request) {
        return ApiResponse.success(chatSessionService.create(request, LoginHelper.getUserId()));
    }

    @Operation(summary = "更新会话")
    @PutMapping("/{id}")
    @RequestLog(title = "更新会话", type = RequestLogType.UPDATE)
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody ChatSessionUpdateRequestDTO request) {
        chatSessionService.update(id, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    @RequestLog(title = "删除会话", type = RequestLogType.DELETE)
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        chatSessionService.delete(id, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
