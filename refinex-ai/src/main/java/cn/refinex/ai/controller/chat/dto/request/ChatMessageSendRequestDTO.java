package cn.refinex.ai.controller.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 发送消息请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "发送消息请求")
public record ChatMessageSendRequestDTO(

        @Schema(description = "会话ID，不传则创建新会话")
        Long sessionId,

        @Schema(description = "Agent ID，新建会话时必传")
        Long agentId,

        @Schema(description = "父消息ID(可选)")
        Long parentMessageId,

        @NotBlank
        @Schema(description = "消息内容")
        String content,

        @Schema(description = "内容格式: TEXT/MARKDOWN/JSON")
        String contentFormat,

        @Schema(description = "附件列表")
        List<ChatAttachmentPayload> attachments
) {
}
