package cn.refinex.ai.controller.chat.dto.request;

import cn.refinex.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话分页请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话分页请求")
public class ChatSessionPageRequest extends PageQuery {

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "是否置顶:1是,0否")
    private Integer pinned;

    @Schema(description = "是否归档:1是,0否")
    private Integer archived;

    @Schema(description = "关键字(标题/摘要)")
    private String keyword;
}
