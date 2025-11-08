package cn.refinex.core.util;

import com.github.promeg.pinyinhelper.Pinyin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 中文拼音转换工具类
 * <p>
 * 提供中文字符串转拼音的功能，包括全拼转换和首字母提取，支持大小写转换、长度限制、自定义分隔符等扩展功能。
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PinyinUtils {

    /**
     * 默认分隔符
     */
    private static final String DEFAULT_SEPARATOR = "";

    /**
     * 将中文字符串转换为全拼音，使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："nihao"
     *
     * @param input 输入的中文字符串
     * @return 全拼音字符串，非中文字符保持原样
     */
    public static String toPinyin(String input) {
        return toPinyin(input, DEFAULT_SEPARATOR);
    }

    /**
     * 将中文字符串转换为全拼音，使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："ni_hao"
     *
     * @param input     输入的中文字符串
     * @param separator 拼音之间的分隔符
     * @return 全拼音字符串
     */
    public static String toPinyin(String input, String separator) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Objects.requireNonNull(separator, "Separator cannot be null");
        return Pinyin.toPinyin(input, separator);
    }

    /**
     * 将中文字符串转换为全拼音（小写），使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："nihao"
     *
     * @param input 输入的中文字符串
     * @return 小写全拼音字符串
     */
    public static String toPinyinLowerCase(String input) {
        return toPinyinLowerCase(input, DEFAULT_SEPARATOR);
    }

    /**
     * 将中文字符串转换为全拼音（小写），使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："ni_hao"
     *
     * @param input     输入的中文字符串
     * @param separator 拼音之间的分隔符
     * @return 小写全拼音字符串
     */
    public static String toPinyinLowerCase(String input, String separator) {
        String pinyin = toPinyin(input, separator);
        return pinyin == null ? null : pinyin.toLowerCase();
    }

    /**
     * 将中文字符串转换为全拼音（大写），使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："NIHAO"
     *
     * @param input 输入的中文字符串
     * @return 大写全拼音字符串
     */
    public static String toPinyinUpperCase(String input) {
        return toPinyinUpperCase(input, DEFAULT_SEPARATOR);
    }

    /**
     * 将中文字符串转换为全拼音（大写），使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："NI_HAO"
     *
     * @param input     输入的中文字符串
     * @param separator 拼音之间的分隔符
     * @return 大写全拼音字符串
     */
    public static String toPinyinUpperCase(String input, String separator) {
        String pinyin = toPinyin(input, separator);
        return pinyin == null ? null : pinyin.toUpperCase();
    }

    /**
     * 将中文字符串转换为全拼音并截取指定长度，使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 最大长度：3
     * 输出："nih"
     *
     * @param input     输入的中文字符串
     * @param maxLength 最大长度
     * @return 截取后的拼音字符串
     */
    public static String toPinyinWithLimit(String input, int maxLength) {
        return toPinyinWithLimit(input, DEFAULT_SEPARATOR, maxLength, false);
    }

    /**
     * 将中文字符串转换为全拼音并截取指定长度，使用指定分隔符
     * <p>
     * 示例1：
     * 分隔符："_"
     * 输入："你好"
     * 最大长度：3
     * 输出："nih"
     * <p>
     * 示例2：
     * 分隔符："_"
     * 输入："你好"
     * 最大长度：3
     * 输出："NI_H"
     *
     * @param input     输入的中文字符串
     * @param separator 拼音之间的分隔符
     * @param maxLength 最大长度
     * @param upperCase 是否转换为大写
     * @return 截取后的拼音字符串
     */
    public static String toPinyinWithLimit(String input, String separator, int maxLength, boolean upperCase) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Max length cannot be negative");
        }

        String pinyin = toPinyin(input, separator);
        if (pinyin == null) {
            return null;
        }

        if (upperCase) {
            pinyin = pinyin.toUpperCase();
        }

        return pinyin.length() > maxLength ? pinyin.substring(0, maxLength) : pinyin;
    }

    /**
     * 提取中文字符串每个汉字的首字母，使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："NH"
     *
     * @param input 输入的中文字符串
     * @return 首字母字符串
     */
    public static String toFirstLetters(String input) {
        return toFirstLetters(input, DEFAULT_SEPARATOR);
    }

    /**
     * 提取中文字符串每个汉字的首字母，使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："N_H"
     *
     * @param input     输入的中文字符串
     * @param separator 首字母之间的分隔符
     * @return 首字母字符串
     */
    public static String toFirstLetters(String input, String separator) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Objects.requireNonNull(separator, "Separator cannot be null");

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Pinyin.isChinese(c)) {
                String pinyin = Pinyin.toPinyin(c);
                if (!pinyin.isEmpty()) {
                    result.append(pinyin.charAt(0));
                }
            } else {
                result.append(c);
            }

            // 添加分隔符（最后一个字符后不加）
            if (i < chars.length - 1 && !separator.isEmpty() && Pinyin.isChinese(c)) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    /**
     * 提取中文字符串每个汉字的首字母（小写），使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："nh"
     *
     * @param input 输入的中文字符串
     * @return 小写首字母字符串
     */
    public static String toFirstLettersLowerCase(String input) {
        return toFirstLettersLowerCase(input, DEFAULT_SEPARATOR);
    }

    /**
     * 提取中文字符串每个汉字的首字母（小写），使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："n_h"
     *
     * @param input     输入的中文字符串
     * @param separator 首字母之间的分隔符
     * @return 小写首字母字符串
     */
    public static String toFirstLettersLowerCase(String input, String separator) {
        String letters = toFirstLetters(input, separator);
        return letters == null ? null : letters.toLowerCase();
    }

    /**
     * 提取中文字符串每个汉字的首字母（大写），使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出："NH"
     *
     * @param input 输入的中文字符串
     * @return 大写首字母字符串
     */
    public static String toFirstLettersUpperCase(String input) {
        return toFirstLettersUpperCase(input, DEFAULT_SEPARATOR);
    }

    /**
     * 提取中文字符串每个汉字的首字母（大写），使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 输出："N_H"
     *
     * @param input     输入的中文字符串
     * @param separator 首字母之间的分隔符
     * @return 大写首字母字符串
     */
    public static String toFirstLettersUpperCase(String input, String separator) {
        String letters = toFirstLetters(input, separator);
        return letters == null ? null : letters.toUpperCase();
    }

    /**
     * 提取中文字符串每个汉字的首字母并截取指定长度，使用默认分隔符
     * <p>
     * 示例：
     * 输入："你好"
     * 最大长度：2
     * 输出："NH"
     *
     * @param input     输入的中文字符串
     * @param maxLength 最大长度
     * @return 截取后的首字母字符串
     */
    public static String toFirstLettersWithLimit(String input, int maxLength) {
        return toFirstLettersWithLimit(input, DEFAULT_SEPARATOR, maxLength, false);
    }

    /**
     * 提取中文字符串每个汉字的首字母并截取指定长度，使用指定分隔符
     * <p>
     * 示例：
     * 分隔符："_"
     * 输入："你好"
     * 最大长度：2
     * 输出："N_H"
     *
     * @param input     输入的中文字符串
     * @param separator 首字母之间的分隔符
     * @param maxLength 最大长度
     * @param upperCase 是否转换为大写
     * @return 截取后的首字母字符串
     */
    public static String toFirstLettersWithLimit(String input, String separator, int maxLength, boolean upperCase) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Max length cannot be negative");
        }

        String letters = toFirstLetters(input, separator);
        if (letters == null) {
            return null;
        }

        if (upperCase) {
            letters = letters.toUpperCase();
        }

        return letters.length() > maxLength ? letters.substring(0, maxLength) : letters;
    }

    /**
     * 判断字符是否为中文字符
     * <p>
     * 示例：
     * 输入：'你'
     * 输出：true
     * <p>
     * 输入：'a'
     * 输出：false
     *
     * @param c 待判断的字符
     * @return true 表示是中文字符，false 表示不是
     */
    public static boolean isChinese(char c) {
        return Pinyin.isChinese(c);
    }

    /**
     * 判断字符串是否包含中文字符
     * <p>
     * 示例：
     * 输入："你好"
     * 输出：true
     * <p>
     * 输入："hello"
     * 输出：false
     *
     * @param input 输入字符串
     * @return true 表示包含中文字符，false 表示不包含
     */
    public static boolean containsChinese(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (char c : input.toCharArray()) {
            if (Pinyin.isChinese(c)) {
                return true;
            }
        }
        return false;
    }
}
