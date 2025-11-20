package cn.refinex.ai.controller.model.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型分页请求")
public class AiModelPageRequest extends PageQuery {

    @Schema(description = "供应商ID")
    private Long providerId;

    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "API 形态")
    private String apiVariant;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "关键字(模型标识/名称模糊搜索)")
    private String keyword;
}
