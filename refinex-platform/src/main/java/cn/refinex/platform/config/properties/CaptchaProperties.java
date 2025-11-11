package cn.refinex.platform.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置属性
 *
 * @author Refinex
 * @since 2025-10-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "refinex.captcha")
public class CaptchaProperties {

    /**
     * 是否启用验证码
     */
    private Boolean enabled = true;

    /**
     * 验证码类型
     * <p>
     * 可选值：
     * - spec: PNG 类型（线段干扰）
     * - gif: GIF 动画类型
     * - arithmetic: 算术验证码
     * - chinese: 中文验证码
     * - chinese_gif: 中文 GIF 验证码
     * </p>
     */
    private String type = "spec";

    /**
     * 图片宽度
     */
    private Integer width = 130;

    /**
     * 图片高度
     */
    private Integer height = 48;

    /**
     * 验证码长度（字符数）
     */
    private Integer length = 4;

    /**
     * 过期时间（秒），默认 300 秒
     */
    private Integer expireSeconds = 300;

    /**
     * 字符类型
     * <p>
     * 可选值：
     * - 0: 数字和字母混合
     * - 1: 纯数字
     * - 2: 纯字母
     * - 3: 纯大写字母
     * - 4: 纯小写字母
     * - 5: 数字和大写字母
     * </p>
     */
    private Integer charType = 2;

    /**
     * 是否区分大小写
     */
    private Boolean caseSensitive = false;

    /**
     * 字体（1-10）
     */
    private Integer font = 1;

    /**
     * Redis Key 前缀
     */
    private String redisKeyPrefix = "captcha:";
}

