package cn.refinex.ai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 调用使用/计费日志（非继承基础字段，保持轻量）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 调用使用/计费日志")
public class AiUsageLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "请求ID/TraceId")
    private String requestId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "供应商ID")
    private Long providerId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型Key")
    private String modelKey;

    @Schema(description = "操作类型: CHAT/EMBEDDING/IMAGE/AUDIO/TOOL/RERANK")
    private String operation;

    @Schema(description = "输入 tokens")
    private Integer inputTokens;

    @Schema(description = "输出 tokens")
    private Integer outputTokens;

    @Schema(description = "花费(统一币种)")
    private BigDecimal cost;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "是否成功:1成功,0失败")
    private Integer success;

    @Schema(description = "HTTP 状态码(如有)")
    private Integer httpStatus;

    @Schema(description = "耗时(ms)")
    private Long latencyMs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

