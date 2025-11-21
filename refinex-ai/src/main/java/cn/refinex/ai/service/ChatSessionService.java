package cn.refinex.ai.service;

import cn.refinex.ai.controller.chat.dto.request.ChatSessionCreateRequestDTO;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionPageRequest;
import cn.refinex.ai.controller.chat.dto.request.ChatSessionUpdateRequestDTO;
import cn.refinex.ai.controller.chat.dto.response.ChatSessionResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * 会话服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ChatSessionService {

    /**
     * 分页查询会话
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    PageResponse<ChatSessionResponseDTO> page(ChatSessionPageRequest request, Long userId);

    /**
     * 根据ID查询会话
     *
     * @param id     会话ID
     * @param userId 用户ID
     * @return 会话响应对象
     */
    Optional<ChatSessionResponseDTO> findById(Long id, Long userId);

    /**
     * 创建会话
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 会话响应对象
     */
    ChatSessionResponseDTO create(ChatSessionCreateRequestDTO request, Long userId);

    /**
     * 更新会话
     *
     * @param id      会话ID
     * @param request 更新请求
     * @param userId  用户ID
     */
    void update(Long id, ChatSessionUpdateRequestDTO request, Long userId);

    /**
     * 删除会话
     *
     * @param id     会话ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);
}
