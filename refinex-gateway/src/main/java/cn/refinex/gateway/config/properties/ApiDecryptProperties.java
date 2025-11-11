package cn.refinex.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * API 解密配置类
 *
 * @author Lion Li
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "api-decrypt")
public class ApiDecryptProperties {

    /**
     * 加密开关，默认关闭
     */
    private boolean enabled = false;

    /**
     * 头部标识，默认使用 Encrypt-Key，检测到该头部则进行解密
     */
    private String headerFlag = "Encrypt-Key";
}
