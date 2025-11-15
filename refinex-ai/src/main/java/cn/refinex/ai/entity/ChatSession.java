package cn.refinex.ai.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话(窗口)
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "聊天会话(窗口)")
public class ChatSession extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会话编码(可用于分享/外部引用)")
    private String sessionCode;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "默认 Agent(逻辑关联 ai_agent.id)")
    private Long agentId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要(用于历史显示)")
    private String summary;

    @Schema(description = "是否置顶:1是,0否")
    private Integer pinned;

    @Schema(description = "是否归档:1是,0否")
    private Integer archived;

    @Schema(description = "消息条数")
    private Integer messageCount;

    @Schema(description = "累计 tokens")
    private Integer tokenCount;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注")
    private String remark;
}

