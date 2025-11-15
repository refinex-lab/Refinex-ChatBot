package cn.refinex.platform.controller.file.dto.request;

/**
 * 更新存储配置请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record StorageConfigUpdateRequestDTO(
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
        /* 存储配置密钥明文 */
        String accessKeyPlain,
        /* 存储配置密钥明文 */
        String secretKeyPlain,
        /* 存储配置会话策略 JSON */
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

