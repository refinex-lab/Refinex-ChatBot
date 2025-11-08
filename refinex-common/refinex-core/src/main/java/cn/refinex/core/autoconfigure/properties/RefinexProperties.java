package cn.refinex.core.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refinex 全局配置属性类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "refinex")
public class RefinexProperties {

    /**
     * AES 加密密钥 (Base64 编码后存放在配置文件)
     * <p>
     * 生成密钥命令（在 Linux / macOS 下）：
     * <pre>{@code
     * openssl rand -base64 32
     * }</pre>
     */
    private String aesKey;

    /**
     * HMAC 密钥 (Base64 编码后存放在配置文件)
     * <p>
     * 生成密钥命令（在 Linux / macOS 下）：
     * <pre>{@code
     * openssl rand -base64 32
     * }</pre>
     */
    private String hmacKey;
}
