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

/**
 * 存储配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "存储配置")
public class SysStorageConfig extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "存储编码，唯一，如 local, s3_main, oss_public")
    private String storageCode;

    @Schema(description = "存储名称")
    private String storageName;

    @Schema(description = "类型: LOCAL/DB/S3/OSS/MINIO/GCS/AZURE")
    private String storageType;

    @Schema(description = "Endpoint")
    private String endpoint;

    @Schema(description = "Region")
    private String region;

    @Schema(description = "Bucket/容器 名称")
    private String bucket;

    @Schema(description = "基础路径/前缀(如 uploads/ )")
    private String basePath;

    @Schema(description = "对外访问基址(如 CDN 域名)")
    private String baseUrl;

    @Schema(description = "AccessKey 密文(AES-GCM)")
    private String accessKeyCipher;

    @Schema(description = "AccessKey 索引/别名(HMAC/KMS别名)")
    private String accessKeyIndex;

    @Schema(description = "SecretKey 密文(AES-GCM)")
    private String secretKeyCipher;

    @Schema(description = "SecretKey 索引/别名")
    private String secretKeyIndex;

    @Schema(description = "会话策略/STS 配置(JSON)")
    private String sessionPolicy;

    @Schema(description = "是否为默认存储:1是,0否")
    private Integer isDefault;

    @Schema(description = "其他扩展配置(JSON)")
    private String extConfig;

    @Schema(description = "状态:1启用,0停用")
    private Integer status;

    @Schema(description = "逻辑删除:0未删除,1已删除")
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}

