package cn.refinex.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储提供方
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StorageProvider {

    LOCAL("local", "本地存储"),
    MINIO("minio", "MinIO"),
    OSS("oss", "阿里云 OSS"),
    S3("s3", "S3/兼容"),
    HTTP("http", "HTTP 远程");

    /**
     * 存储提供方代码
     */
    private final String code;

    /**
     * 存储提供方描述
     */
    private final String description;

    /**
     * 根据代码获取存储提供方
     *
     * @param code 代码
     * @return 存储提供方
     */
    public static StorageProvider fromCode(String code) {
        for (StorageProvider t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown StorageProvider: " + code);
    }
}

