package cn.refinex.core.util;

import cn.hutool.core.util.ReUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 正则表达式工具类
 * <p>
 * 提供正则表达式相关的匹配和提取功能, 继承自 Hutool 的 {@link ReUtil},
 * 提供增强的异常处理和默认值支持。
 *
 * @author refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegexUtils extends ReUtil {

    /**
     * 从输入字符串中提取匹配的分组内容
     * <p>
     * 根据正则表达式从输入字符串中提取第一个匹配的分组内容,
     * 如果没有匹配或发生异常,则返回指定的默认值。
     *
     * @param input        待匹配的输入字符串
     * @param regex        正则表达式模式
     * @param defaultValue 未匹配时返回的默认值
     * @return 匹配的分组内容,如果未匹配则返回默认值
     */
    public static String extractOrDefault(String input, String regex, String defaultValue) {
        try {
            String result = ReUtil.get(regex, input, 1);
            return result == null ? defaultValue : result;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
