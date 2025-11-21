package cn.refinex.ai.controller.chat.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 使用日志分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "使用日志分页请求")
public class AiUsageLogPageRequest extends PageQuery {

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "是否成功")
    private Boolean success;
}
