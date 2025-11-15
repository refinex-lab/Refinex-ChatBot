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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会话消息
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话消息")
public class ChatMessage extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会话ID(逻辑关联 chat_session.id)")
    private Long sessionId;

    @Schema(description = "父消息ID(用于树/线程)")
    private Long parentMessageId;

    @Schema(description = "角色: system/user/assistant/tool")
    private String role;

    @Schema(description = "消息类型: NORMAL/ERROR/TOOL_CALL/TOOL_RESULT/EVENT")
    private String messageType;

    @Schema(description = "文本内容")
    private String contentText;

    @Schema(description = "内容格式: TEXT/MARKDOWN/JSON")
    private String contentFormat;

    @Schema(description = "结构化内容(JSON)")
    private String contentJson;

    @Schema(description = "附件数量")
    private Integer attachmentsCount;

    @Schema(description = "工具调用请求(JSON)")
    private String toolCalls;

    @Schema(description = "工具调用结果(JSON)")
    private String toolResults;

    @Schema(description = "供应商ID(逻辑关联 ai_provider.id)")
    private Long providerId;

    @Schema(description = "模型ID(逻辑关联 ai_model.id)")
    private Long modelId;

    @Schema(description = "结束原因: stop/length/tool_calls/other")
    private String finishReason;

    @Schema(description = "提示 tokens")
    private Integer inputTokens;

    @Schema(description = "补全 tokens")
    private Integer outputTokens;

    @Schema(description = "耗时(ms)")
    private Long latencyMs;

    @Schema(description = "花费(转换成统一币种,如 USD)")
    private BigDecimal cost;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "消息时间")
    private LocalDateTime messageTime;

    @Schema(description = "状态:1正常,0异常")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

