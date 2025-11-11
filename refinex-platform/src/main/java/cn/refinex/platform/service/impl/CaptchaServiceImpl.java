package cn.refinex.platform.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.platform.config.properties.CaptchaProperties;
import cn.refinex.platform.controller.captcha.dto.response.CaptchaCreateResponseDTO;
import cn.refinex.platform.enums.CaptchaType;
import cn.refinex.platform.service.CaptchaService;
import cn.refinex.redis.core.RedisService;
import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author Refinex
 * @since 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final CaptchaProperties captchaProperties;
    private final RedisService redisService;

    /**
     * 生成验证码
     *
     * @return 验证码生成响应
     */
    @Override
    public CaptchaCreateResponseDTO generate() {
        try {
            // 生成验证码对象
            Captcha captcha = createCaptcha();
            String uuid = RandomUtil.randomString(32);

            // 获取验证码文本, 处理大小写转换，全部转为小写
            String code = captcha.text();
            if (Boolean.FALSE.equals(captchaProperties.getCaseSensitive())) {
                code = code.toLowerCase();
            }

            // 存储到 Redis，默认 300 秒过期
            String redisKey = captchaProperties.getRedisKeyPrefix() + uuid;
            redisService.string().set(redisKey, code, captchaProperties.getExpireSeconds(), TimeUnit.SECONDS);

            // 生成 Base64 图片
            return CaptchaCreateResponseDTO.builder()
                    .uuid(uuid)
                    .image(captcha.toBase64())
                    .expireSeconds(captchaProperties.getExpireSeconds())
                    .build();

        } catch (Exception e) {
            log.error("生成验证码失败", e);
            throw new BusinessException("生成验证码失败", e);
        }
    }

    /**
     * 验证验证码
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码文本
     * @throws BusinessException 验证失败时抛出异常
     */
    @Override
    public void verify(String uuid, String code) {
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(code)) {
            throw new BusinessException("验证码不能为空");
        }

        // 从 Redis 中获取验证码
        String redisKey = captchaProperties.getRedisKeyPrefix() + uuid;
        String correctCode = redisService.string().get(redisKey, String.class);
        if (!StringUtils.hasText(correctCode)) {
            log.warn("验证码已过期或不存在，uuid={}", uuid);
            throw new BusinessException("验证码已过期");
        }

        // 比对验证码（兼容大小写配置规则）
        String inputCode = Boolean.TRUE.equals(captchaProperties.getCaseSensitive()) ? code : code.toLowerCase();
        if (!correctCode.equals(inputCode)) {
            log.warn("验证码不正确，uuid={}, input={}, correct={}", uuid, inputCode, correctCode);
            throw new BusinessException("验证码错误");
        }

        // 验证成功，删除验证码（一次性使用）
        redisService.delete(redisKey);
    }

    /**
     * 创建验证码对象
     *
     * @return 验证码对象
     */
    private Captcha createCaptcha() throws IOException, FontFormatException {
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        int length = captchaProperties.getLength();
        String type = captchaProperties.getType();

        // 根据类型创建验证码
        CaptchaType typeEnum = CaptchaType.fromCode(type);
        Captcha captcha;

        switch (typeEnum) {
            case GIF:
                captcha = new GifCaptcha(width, height, length);
                break;
            case ARITHMETIC:
                ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(width, height);
                // 几位数运算
                arithmeticCaptcha.setLen(length);
                captcha = arithmeticCaptcha;
                break;
            case CHINESE:
                captcha = new ChineseCaptcha(width, height, length);
                break;
            case CHINESE_GIF:
                captcha = new ChineseGifCaptcha(width, height, length);
                break;
            case SPEC:
            default:
                captcha = new SpecCaptcha(width, height, length);
                break;
        }

        // 设置字符类型（仅对 SpecCaptcha 和 GifCaptcha 有效）
        if (captcha instanceof SpecCaptcha || captcha instanceof GifCaptcha) {
            captcha.setCharType(captchaProperties.getCharType());
        }

        // 设置字体
        captcha.setFont(captchaProperties.getFont());

        return captcha;
    }
}

