package cn.refinex.ai.controller.advisor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Advisor 响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "Advisor 响应对象")
public class AiAdvisorResponseDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "编码")
    private String advisorCode;

    @Schema(description = "名称")
    private String advisorName;

    @Schema(description = "类型")
    private String advisorType;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
