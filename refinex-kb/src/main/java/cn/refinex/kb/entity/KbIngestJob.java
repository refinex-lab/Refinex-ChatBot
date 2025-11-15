package cn.refinex.kb.entity;

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
 * 知识入库/索引任务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识入库/索引任务")
public class KbIngestJob extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "知识库ID")
    private Long kbId;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "任务类型: INGEST/REINDEX/DELETE/REFRESH")
    private String jobType;

    @Schema(description = "状态: PENDING/RUNNING/DONE/FAILED")
    private String status;

    @Schema(description = "进度百分比 0-100")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "开始时间")
    private LocalDateTime startedTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishedTime;

    @Schema(description = "扩展参数/上下文(JSON)")
    private String extra;
}

