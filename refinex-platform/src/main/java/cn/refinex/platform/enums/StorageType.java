package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储类型
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StorageType {

    LOCAL("LOCAL", "本地磁盘"),
    DB("DB", "数据库"),
    S3("S3", "Amazon S3/兼容"),
    OSS("OSS", "阿里云 OSS"),
    MINIO("MINIO", "MinIO"),
    GCS("GCS", "Google Cloud Storage"),
    AZURE("AZURE", "Azure Blob");

    /**
     * 存储类型代码
     */
    private final String code;

    /**
     * 存储类型描述
     */
    private final String description;

    /**
     * 根据代码获取存储类型
     *
     * @param code 存储类型代码
     * @return 存储类型
     */
    public static StorageType fromCode(String code) {
        for (StorageType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown StorageType: " + code);
    }
}

