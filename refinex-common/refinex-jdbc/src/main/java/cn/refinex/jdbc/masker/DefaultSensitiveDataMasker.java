package cn.refinex.jdbc.masker;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认敏感数据掩码器(脱敏)实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
public class DefaultSensitiveDataMasker implements SensitiveDataMasker {

    /**
     * 敏感关键字集合
     */
    private final Set<String> sensitiveKeys;

    /**
     * 敏感模式集合
     */
    private final Set<String> sensitivePatterns;

    /**
     * 脱敏掩码
     */
    private final String maskValue;

    /**
     * 构造函数, 默认脱敏掩码为 "******"
     *
     * @param sensitiveKeys     敏感关键字列表
     * @param sensitivePatterns 敏感模式列表
     */
    public DefaultSensitiveDataMasker(List<String> sensitiveKeys, List<String> sensitivePatterns) {
        this(sensitiveKeys, sensitivePatterns, "******");
    }

    /**
     * 构造函数, 自定义脱敏掩码
     *
     * @param sensitiveKeys     敏感关键字列表
     * @param sensitivePatterns 敏感模式列表
     * @param maskValue         脱敏掩码
     */
    public DefaultSensitiveDataMasker(List<String> sensitiveKeys, List<String> sensitivePatterns, String maskValue) {
        this.sensitiveKeys = new HashSet<>();
        this.sensitivePatterns = new HashSet<>();
        this.maskValue = maskValue;

        if (CollectionUtils.isNotEmpty(sensitiveKeys)) {
            // 转换为小写, 忽略大小写
            sensitiveKeys.forEach(key -> this.sensitiveKeys.add(key.toLowerCase()));
        }

        if (CollectionUtils.isNotEmpty(sensitivePatterns)) {
            // 转换为小写, 忽略大小写
            sensitivePatterns.forEach(pattern -> this.sensitivePatterns.add(pattern.toLowerCase()));
        }
    }

    /**
     * 判断键是否为敏感键
     *
     * @param key 键
     * @return 是否为敏感键
     */
    @Override
    public boolean isSensitive(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }

        // 因为构造时约定了小写 key, 这里也转换为小写, 忽略大小写
        String lowerKey = key.toLowerCase();
        if (sensitiveKeys.contains(lowerKey)) {
            return true;
        }

        for (String pattern : sensitivePatterns) {
            // 包含敏感模式, 则为敏感键
            if (lowerKey.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 脱敏处理
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    @Override
    public Object mask(Object value) {
        return maskValue;
    }
}
