package cn.refinex.ai.controller.advisor.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Advisor 分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Advisor 分页请求")
public class AiAdvisorPageRequest extends PageQuery {

    @Schema(description = "类型")
    private String advisorType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键字")
    private String keyword;
}
