package cn.refinex.ai.service;

import cn.refinex.ai.controller.chat.dto.request.ChatMessagePageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatMessageSendRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatMessageResponseDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatMessageSendResponseDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatStreamEventDTO;
import cn.refinex.core.api.PageResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天消息服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ChatMessageService {

    /**
     * 分页查询聊天消息
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    PageResponse<ChatMessageResponseDTO> page(ChatMessagePageRequest request, Long userId);

    /**
     * 发送聊天消息
     *
     * @param request 发送请求
     * @param userId  用户ID
     * @return 发送响应
     */
    ChatMessageSendResponseDTO send(ChatMessageSendRequestDTO request, Long userId);

    /**
     * 流式发送聊天消息
     *
     * @param request 发送请求
     * @param userId  用户ID
     * @return 流式事件响应
     */
    Flux<ChatStreamEventDTO> stream(ChatMessageSendRequestDTO request, Long userId);
}
