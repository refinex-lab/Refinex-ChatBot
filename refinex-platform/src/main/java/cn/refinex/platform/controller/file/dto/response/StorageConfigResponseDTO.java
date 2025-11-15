package cn.refinex.platform.controller.file.dto.response;

/**
 * 存储配置响应（屏蔽敏感字段，仅提供是否存在标记）
 *
 * @author Refinex
 * @since 1.0.0
 */
public record StorageConfigResponseDTO(
        /* 存储配置编码 */
        String storageCode,
        /* 存储配置名称 */
        String storageName,
        /* 存储配置类型 */
        String storageType,
        /* 存储配置地址 */
        String endpoint,
        /* 存储配置区域 */
        String region,
        /* 存储配置桶 */
        String bucket,
        /* 存储配置基础路径 */
        String basePath,
        /* 存储配置基础 URL */
        String baseUrl,
        /* 存储配置 AccessKey */
        boolean hasAccessKey,
        /* 存储配置 SecretKey */
        boolean hasSecretKey,
        /* 存储配置 Session 策略 JSON */
        String sessionPolicy,
        /* 存储配置是否默认 */
        Integer isDefault,
        /* 存储配置扩展配置 JSON */
        String extConfig,
        /* 存储配置状态 */
        Integer status,
        /* 存储配置备注 */
        String remark
) {}

