package cn.refinex.platform.entity;

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
 * 系统文件元信息
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统文件元信息")
public class SysFile extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "存储编码(逻辑关联 sys_storage_config.storage_code)")
    private String storageCode;

    @Schema(description = "对象键/路径(对于对象存储)")
    private String fileKey;

    @Schema(description = "可访问 URI(如 s3://bucket/key 或 https://...)")
    private String uri;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "扩展名")
    private String ext;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件大小")
    private Long sizeBytes;

    @Schema(description = "SHA-256 校验和")
    private String checksumSha256;

    @Schema(description = "图片宽(像素)")
    private Integer width;

    @Schema(description = "图片高(像素)")
    private Integer height;

    @Schema(description = "音视频时长(ms)")
    private Long durationMs;

    @Schema(description = "加密算法: NONE/AES256/KMS等")
    private String encryptAlgo;

    @Schema(description = "是否存储在数据库:1是,0否")
    private Integer isDbStored;

    @Schema(description = "业务类型: CHAT_MSG/USER_AVATAR/DOC/KB/OTHER")
    private String bizType;

    @Schema(description = "业务ID(字符串，兼容多种主键)")
    private String bizId;

    @Schema(description = "标题/说明")
    private String title;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态:1正常,0禁用")
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

