package cn.refinex.ai.entity;

import cn.refinex.jdbc.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提示词主表
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "提示词主表")
public class AiPrompt extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "提示词编码，唯一")
    private String promptCode;

    @Schema(description = "提示词名称")
    private String promptName;

    @Schema(description = "分类: general/rag/agent/test 等")
    private String category;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "模板格式: SPRING/MUSTACHE/STRING/LITERAL")
    private String templateFormat;

    @Schema(description = "默认角色: system/user/assistant/tool")
    private String role;

    @Schema(description = "模板正文")
    private String template;

    @Schema(description = "变量示例/默认值(JSON)")
    private String variables;

    @Schema(description = "Few-Shot 示例(JSON)")
    private String examples;

    @Schema(description = "内容摘要(用于防重)")
    private String hashSha256;

    @Schema(description = "输入参数 JSON-Schema")
    private String inputSchema;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "删除人ID")
    private Long deleteBy;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "备注")
    private String remark;
}

