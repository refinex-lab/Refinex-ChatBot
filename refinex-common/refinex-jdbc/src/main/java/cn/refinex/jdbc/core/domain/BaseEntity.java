package cn.refinex.jdbc.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder // Lombok 1.18.2+ 开始支持（建议至少用 1.18.24 以上版本）, 支持继承字段
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
