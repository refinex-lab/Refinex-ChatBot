package cn.refinex.platform.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录安全配置
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "refinex.auth.login")
public class AuthLoginProperties {

    /**
     * 是否开启登录验证码校验
     */
    private boolean enableCaptcha = true;

    /**
     * 是否允许注册
     */
    private boolean allowRegister = true;

    /**
     * 登录失败达到多少次后锁定
     */
    private int maxFailAttempts = 5;

    /**
     * 登录失败计数的有效时间窗口，超过该时间窗口后失败次数会被重置，默认 15 分钟
     */
    private Duration failRecordTtl = Duration.ofMinutes(15);

    /**
     * 触发锁定后的锁定时长，默认 30 分钟
     */
    private Duration lockDuration = Duration.ofMinutes(30);
}
