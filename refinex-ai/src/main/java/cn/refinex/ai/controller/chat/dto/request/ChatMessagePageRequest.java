package cn.refinex.ai.controller.chat.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息分页请求")
public class ChatMessagePageRequest extends PageQuery {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long sessionId;
}
