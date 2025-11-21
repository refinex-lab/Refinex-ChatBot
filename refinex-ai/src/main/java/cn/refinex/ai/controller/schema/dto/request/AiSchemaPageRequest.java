package cn.refinex.ai.controller.schema.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Schema 分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Schema 分页请求")
public class AiSchemaPageRequest extends PageQuery {

    @Schema(description = "类型")
    private String schemaType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键字")
    private String keyword;
}
