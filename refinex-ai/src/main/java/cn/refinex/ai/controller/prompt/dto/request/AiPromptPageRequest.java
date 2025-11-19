package cn.refinex.ai.controller.prompt.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提示词分页查询请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "提示词分页查询请求")
public class AiPromptPageRequest extends PageQuery {

    @Schema(description = "分类: general/rag/agent/test")
    private String category;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "模板格式: SPRING/MUSTACHE/STRING/LITERAL")
    private String templateFormat;

    @Schema(description = "关键字（编码/名称/描述模糊匹配）")
    private String keyword;
}
