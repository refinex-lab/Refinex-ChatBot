package cn.refinex.ai.controller.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用日志响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "使用日志响应")
public class AiUsageLogResponseDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "请求ID")
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

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "输入 tokens")
    private Integer inputTokens;

    @Schema(description = "输出 tokens")
    private Integer outputTokens;

    @Schema(description = "花费")
    private BigDecimal cost;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "是否成功")
    private Integer success;

    @Schema(description = "HTTP 状态")
    private Integer httpStatus;

    @Schema(description = "耗时(ms)")
    private Long latencyMs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
