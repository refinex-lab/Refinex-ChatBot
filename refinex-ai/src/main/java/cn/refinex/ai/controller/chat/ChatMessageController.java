package cn.refinex.ai.controller.chat;

import cn.refinex.ai.controller.chat.dto.request.ChatMessagePageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatMessageSendRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatMessageResponseDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatMessageSendResponseDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatStreamEventDTO;
import cn.refinex.ai.service.ChatMessageService;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.logging.annotation.RequestLog;
import cn.refinex.core.logging.enums.RequestLogType;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 消息接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/chat/messages")
@Tag(name = "Chat Message", description = "聊天消息管理")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "分页查询会话消息")
    @GetMapping
    public ApiResponse<PageResponse<ChatMessageResponseDTO>> page(@Valid ChatMessagePageRequest request) {
        return ApiResponse.success(chatMessageService.page(request, LoginHelper.getUserId()));
    }

    @Operation(summary = "发送消息并获取回复")
    @PostMapping
    @RequestLog(title = "发送聊天消息", type = RequestLogType.CREATE)
    public ApiResponse<ChatMessageSendResponseDTO> send(@Valid @RequestBody ChatMessageSendRequestDTO request) {
        return ApiResponse.success(chatMessageService.send(request, LoginHelper.getUserId()));
    }

    @Operation(summary = "流式发送消息")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEventDTO>> stream(@Valid @RequestBody ChatMessageSendRequestDTO request) {
        Long userId = LoginHelper.getUserId();
        return chatMessageService.stream(request, userId)
                .map(event -> ServerSentEvent.<ChatStreamEventDTO>builder()
                        .event(event.getEvent())
                        .data(event)
                        .build());
    }
}
