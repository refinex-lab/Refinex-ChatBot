package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "会话响应")
public class ChatSessionResponseDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会话编码")
    private String sessionCode;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "Agent ID")
    private Long agentId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "是否置顶")
    private Integer pinned;

    @Schema(description = "是否归档")
    private Integer archived;

    @Schema(description = "消息数")
    private Integer messageCount;

    @Schema(description = "累计 tokens")
    private Integer tokenCount;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
