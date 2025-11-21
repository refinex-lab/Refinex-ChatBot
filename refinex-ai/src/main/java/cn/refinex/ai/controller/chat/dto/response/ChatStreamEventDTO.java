package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 流式输出事件
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天流式事件")
public class ChatStreamEventDTO {

    @Schema(description = "事件类型: session/delta/complete/error")
    private String event;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "是否新会话")
    private Boolean newSession;

    @Schema(description = "内容增量")
    private String delta;

    @Schema(description = "段类型")
    private String segmentType;

    @Schema(description = "是否结束")
    private Boolean finished;

    @Schema(description = "完整消息")
    private ChatMessageResponseDTO message;

    @Schema(description = "附加元数据")
    private Map<String, Object> metadata;
}
