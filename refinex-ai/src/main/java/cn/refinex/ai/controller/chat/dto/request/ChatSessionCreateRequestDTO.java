package cn.refinex.ai.controller.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建会话请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "创建会话请求")
public record ChatSessionCreateRequestDTO(

        @NotNull(message = "默认 Agent ID不能为空")
        @Schema(description = "默认 Agent ID")
        Long agentId,

        @Size(max = 200, message = "会话标题长度不能超过200个字符")
        @Schema(description = "会话标题")
        String title
) {
}
