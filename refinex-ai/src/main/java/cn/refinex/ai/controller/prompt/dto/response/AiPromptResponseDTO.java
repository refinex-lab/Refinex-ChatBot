package cn.refinex.ai.controller.prompt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 提示词响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "提示词响应")
public record AiPromptResponseDTO(

        @Schema(description = "主键ID")
        Long id,

        @Schema(description = "提示词编码")
        String promptCode,

        @Schema(description = "提示词名称")
        String promptName,

        @Schema(description = "分类")
        String category,

        @Schema(description = "描述")
        String description,

        @Schema(description = "模板格式")
        String templateFormat,

        @Schema(description = "默认角色")
        String role,

        @Schema(description = "模板正文")
        String template,

        @Schema(description = "变量示例/默认值(JSON)")
        Map<String, Object> variables,

        @Schema(description = "Few-Shot 示例(JSON)")
        List<Map<String, Object>> examples,

        @Schema(description = "输入参数 JSON-Schema")
        Map<String, Object> inputSchema,

        @Schema(description = "内容摘要(用于防重)")
        String hashSha256,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Schema(description = "备注")
        String remark,

        @Schema(description = "创建人")
        Long createBy,

        @Schema(description = "创建时间")
        LocalDateTime createTime,

        @Schema(description = "更新人")
        Long updateBy,

        @Schema(description = "更新时间")
        LocalDateTime updateTime,

        @Schema(description = "删除人")
        Long deleteBy,

        @Schema(description = "删除时间")
        LocalDateTime deleteTime
) {
}
