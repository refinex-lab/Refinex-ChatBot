package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "消息响应")
public class ChatMessageResponseDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "父消息ID")
    private Long parentMessageId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "内容格式")
    private String contentFormat;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "内容段")
    private java.util.List<ChatMessageSegmentDTO> segments;

    @Schema(description = "结构化内容")
    private Map<String, Object> contentJson;

    @Schema(description = "工具调用")
    private Map<String, Object> toolCalls;

    @Schema(description = "工具结果")
    private Map<String, Object> toolResults;

    @Schema(description = "Attachments")
    private List<ChatAttachmentResponseDTO> attachments;

    @Schema(description = "供应商ID")
    private Long providerId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "结束原因")
    private String finishReason;

    @Schema(description = "输入 tokens")
    private Integer inputTokens;

    @Schema(description = "输出 tokens")
    private Integer outputTokens;

    @Schema(description = "耗时(ms)")
    private Long latencyMs;

    @Schema(description = "花费")
    private BigDecimal cost;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "消息时间")
    private LocalDateTime messageTime;
}
