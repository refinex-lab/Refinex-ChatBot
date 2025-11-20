package cn.refinex.ai.controller.provider.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型供应商分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型供应商分页请求")
public class AiProviderPageRequest extends PageQuery {

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "关键字(编码/名称模糊匹配)")
    private String keyword;
}
