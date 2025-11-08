package cn.refinex.jdbc.masker;

/**
 * 敏感数据掩码器(脱敏)接口
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface SensitiveDataMasker {

    /**
     * 判断键是否为敏感键
     *
     * @param key 键
     * @return 是否为敏感键
     */
    boolean isSensitive(String key);

    /**
     * 脱敏处理
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    Object mask(Object value);
}
