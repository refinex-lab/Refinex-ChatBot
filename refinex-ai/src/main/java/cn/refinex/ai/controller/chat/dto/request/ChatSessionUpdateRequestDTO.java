package cn.refinex.ai.controller.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 会话更新请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "会话更新请求")
public record ChatSessionUpdateRequestDTO(

        @Size(max = 200, message = "会话标题长度不能超过200个字符")
        @Schema(description = "会话标题")
        String title,

        @Schema(description = "是否置顶:1是,0否")
        Integer pinned,

        @Schema(description = "是否归档:1是,0否")
        Integer archived,

        @Schema(description = "状态:1启用,0停用")
        Integer status
) {
}
