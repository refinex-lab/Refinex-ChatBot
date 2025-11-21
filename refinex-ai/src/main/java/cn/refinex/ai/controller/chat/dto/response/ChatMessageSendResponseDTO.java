package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送消息响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "发送消息响应")
public class ChatMessageSendResponseDTO {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "是否新会话")
    private boolean newSession;

    @Schema(description = "用户消息")
    private ChatMessageResponseDTO userMessage;

    @Schema(description = "助手消息")
    private ChatMessageResponseDTO assistantMessage;
}
