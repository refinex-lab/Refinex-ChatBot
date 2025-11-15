package cn.refinex.platform.controller.file.dto.request;

/**
 * 创建存储配置请求
 *
 * @author Refinex
 * @since 1.0.0
 */
public record StorageConfigCreateRequestDTO(
        /* 存储编码 */
        String storageCode,
        /* 存储名称 */
        String storageName,
        /* 存储类型 */
        String storageType,
        /* 端点 */
        String endpoint,
        /* 地域 */
        String region,
        /* 存储桶 */
        String bucket,
        /* 基础路径 */
        String basePath,
        /* 基础 URL */
        String baseUrl,
        /* 访问密钥 */
        String accessKeyPlain,
        /* 密钥密钥 */
        String secretKeyPlain,
        /* 会话策略 JSON */
        String sessionPolicy,
        /* 是否默认 */
        Integer isDefault,
        /* 扩展配置 JSON */
        String extConfig,
        /* 状态 */
        Integer status,
        /* 备注 */
        String remark
) {}

