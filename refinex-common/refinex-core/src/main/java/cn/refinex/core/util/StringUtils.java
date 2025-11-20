package cn.refinex.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

/**
 * 字符串工具类
 * <p>
 * 提供常用的字符串处理功能,包括空值判断、格式转换、字符串切分、
 * 路径匹配等操作。继承自 Apache Commons Lang 的 {@link StringUtils}, 增强部分功能。
 *
 * @author refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 默认分隔符:逗号
     */
    public static final String SEPARATOR = ",";

    /**
     * 斜杠分隔符
     */
    public static final String SLASH = "/";

    /**
     * 路径匹配器
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 获取参数不为空值
     * <p>
     * 如果字符串为空白,则返回默认值,否则返回原值。
     *
     * @param str          要判断的字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String blankToDefault(String str, String defaultValue) {
        return StrUtil.blankToDefault(str, defaultValue);
    }

    /**
     * 判断字符串是否为空串
     *
     * @param str 待判断的字符串
     * @return 如果为null或空字符串返回true,否则返回false
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否为非空串
     *
     * @param str 待判断的字符串
     * @return 如果不为null且不为空字符串返回true,否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去除字符串首尾空白字符
     *
     * @param str 待处理的字符串
     * @return 去除空白后的字符串
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    /**
     * 修剪字符串并转换为空字符串（null）
     *
     * @param text 文本
     * @return 修剪后的字符串（null）
     */
    public static String trimToNull(String text) {
        if (isBlank(text)) {
            return null;
        }
        return text.trim();
    }

    /**
     * 截取字符串
     *
     * @param str   原字符串
     * @param start 开始位置(包含)
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start) {
        return substring(str, start, str.length());
    }

    /**
     * 截取字符串
     *
     * @param str   原字符串
     * @param start 开始位置(包含)
     * @param end   结束位置(不包含)
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start, int end) {
        return StrUtil.sub(str, start, end);
    }

    /**
     * 格式化文本,使用{}作为占位符
     * <p>
     * 按顺序将占位符{}替换为参数值。支持转义:使用反斜杠转义{来输出{}。
     *
     * @param template 文本模板,使用{}表示占位符
     * @param params   参数值数组
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }

    /**
     * 判断是否为HTTP或HTTPS开头的URL
     *
     * @param link 链接地址
     * @return 如果是有效的URL返回true,否则返回false
     */
    public static boolean isHttp(String link) {
        return Validator.isUrl(link);
    }

    /**
     * 将字符串转换为Set集合
     *
     * @param str       待转换的字符串
     * @param separator 分隔符
     * @return 转换后的Set集合
     */
    public static Set<String> str2Set(String str, String separator) {
        return new HashSet<>(str2List(str, separator, true, false));
    }

    /**
     * 将字符串转换为List集合
     *
     * @param str         待转换的字符串
     * @param separator   分隔符
     * @param filterBlank 是否过滤空白字符串
     * @param trim        是否去除首尾空白
     * @return 转换后的List集合
     */
    public static List<String> str2List(String str, String separator, boolean filterBlank, boolean trim) {
        List<String> result = new ArrayList<>();
        if (isEmpty(str)) {
            return result;
        }

        if (filterBlank && isBlank(str)) {
            return result;
        }

        String[] parts = str.split(separator);
        for (String part : parts) {
            if (filterBlank && isBlank(part)) {
                continue;
            }
            if (trim) {
                part = trim(part);
            }
            result.add(part);
        }

        return result;
    }

    /**
     * 检查字符串是否包含任意一个指定的子串(忽略大小写)
     *
     * @param cs                  待检查的字符串
     * @param searchCharSequences 要查找的子串数组
     * @return 如果包含任意一个子串返回true,否则返回false
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences) {
        return StrUtil.containsAnyIgnoreCase(cs, searchCharSequences);
    }

    /**
     * 将驼峰命名转换为下划线命名
     *
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String toUnderScoreCase(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 检查字符串是否等于任意一个指定的字符串(忽略大小写)
     *
     * @param str  待检查的字符串
     * @param strs 要比较的字符串数组
     * @return 如果相等返回true,否则返回false
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        return StrUtil.equalsAnyIgnoreCase(str, strs);
    }

    /**
     * 将下划线大写命名转换为帕斯卡命名(首字母大写的驼峰命名)
     * <p>
     * 例如:HELLO_WORLD转换为HelloWorld
     *
     * @param name 下划线大写命名的字符串
     * @return 帕斯卡命名的字符串
     */
    public static String convertToCamelCase(String name) {
        return StrUtil.upperFirst(StrUtil.toCamelCase(name));
    }

    /**
     * 将下划线命名转换为驼峰命名
     * <p>
     * 例如:user_name转换为userName
     *
     * @param str 下划线命名的字符串
     * @return 驼峰命名的字符串
     */
    public static String toCamelCase(String str) {
        return StrUtil.toCamelCase(str);
    }

    /**
     * 检查字符串是否匹配列表中的任意一个模式
     *
     * @param str      待检查的字符串
     * @param patterns 模式列表
     * @return 如果匹配任意一个模式返回true,否则返回false
     */
    public static boolean matches(String str, List<String> patterns) {
        if (isEmpty(str) || CollUtil.isEmpty(patterns)) {
            return false;
        }
        for (String pattern : patterns) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断URL是否匹配指定的Ant风格路径模式
     * <p>
     * 支持的通配符:
     * <ul>
     *   <li>? 表示单个字符</li>
     *   <li>* 表示一层路径内的任意字符串</li>
     *   <li>** 表示任意层路径</li>
     * </ul>
     *
     * @param pattern 匹配模式
     * @param url     待匹配的URL
     * @return 如果匹配返回true,否则返回false
     */
    public static boolean isMatch(String pattern, String url) {
        return PATH_MATCHER.match(pattern, url);
    }

    /**
     * 数字左侧补零至指定长度
     * <p>
     * 如果数字转换为字符串后长度大于指定长度,则只保留最后指定长度的字符。
     *
     * @param num  数字对象
     * @param size 目标字符串长度
     * @return 补零后的字符串
     */
    public static String padLeft(final Number num, final int size) {
        return padLeft(num.toString(), size, '0');
    }

    /**
     * 字符串左侧补齐指定字符至指定长度
     * <p>
     * 如果原字符串长度大于指定长度,则只保留最后指定长度的字符。
     *
     * @param str     原字符串
     * @param size    目标字符串长度
     * @param padChar 用于补齐的字符
     * @return 补齐后的字符串
     */
    public static String padLeft(final String str, final int size, final char padChar) {
        final StringBuilder sb = new StringBuilder(size);
        if (str != null) {
            final int len = str.length();
            if (len <= size) {
                sb.append(String.valueOf(padChar).repeat(size - len));
                sb.append(str);
            } else {
                return str.substring(len - size, len);
            }
        } else {
            sb.append(String.valueOf(padChar).repeat(Math.max(0, size)));
        }
        return sb.toString();
    }

    /**
     * 切分字符串为List(使用默认分隔符逗号)
     *
     * @param str 待切分的字符串
     * @return 切分后的字符串列表
     */
    public static List<String> splitList(String str) {
        return splitTo(str, Convert::toStr);
    }

    /**
     * 切分字符串为List
     *
     * @param str       待切分的字符串
     * @param separator 分隔符
     * @return 切分后的字符串列表
     */
    public static List<String> splitList(String str, String separator) {
        return splitTo(str, separator, Convert::toStr);
    }

    /**
     * 切分字符串并自定义转换(使用默认分隔符逗号)
     *
     * @param str    待切分的字符串
     * @param mapper 自定义转换函数
     * @param <T>    目标类型
     * @return 切分并转换后的列表
     */
    public static <T> List<T> splitTo(String str, Function<? super Object, T> mapper) {
        return splitTo(str, SEPARATOR, mapper);
    }

    /**
     * 切分字符串并自定义转换
     *
     * @param str       待切分的字符串
     * @param separator 分隔符
     * @param mapper    自定义转换函数
     * @param <T>       目标类型
     * @return 切分并转换后的列表
     */
    public static <T> List<T> splitTo(String str, String separator, Function<? super Object, T> mapper) {
        if (isBlank(str)) {
            return new ArrayList<>(0);
        }
        return StrUtil.split(str, separator)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 检查字符串是否以任意一个指定前缀开头(忽略大小写)
     * <p>
     * 该方法对null值进行安全处理,不会抛出异常。如果待检查的字符串为null或前缀数组为空,
     * 则返回false。比较时忽略大小写。
     * <p>
     * 示例:
     * <pre>
     * StringUtils.startWithAnyIgnoreCase(null, "http", "https")           = false
     * StringUtils.startWithAnyIgnoreCase("", "http", "https")             = false
     * StringUtils.startWithAnyIgnoreCase("https://example.com", "http")   = false
     * StringUtils.startWithAnyIgnoreCase("https://example.com", "https")  = true
     * StringUtils.startWithAnyIgnoreCase("HTTP://example.com", "http")    = true
     * StringUtils.startWithAnyIgnoreCase("ftp://server.com", "http", "https", "ftp") = true
     * </pre>
     *
     * @param str      待检查的字符串,可以为null
     * @param prefixes 前缀数组,可以包含null元素
     * @return 如果字符串以任意一个前缀开头(忽略大小写)返回true,否则返回false
     */
    public static boolean startWithAnyIgnoreCase(CharSequence str, CharSequence... prefixes) {
        if (isEmpty(str) || prefixes == null) {
            return false;
        }

        for (CharSequence prefix : prefixes) {
            if (prefix != null && regionMatches(str, prefix, prefix.length())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串区域匹配比较
     * <p>
     * 该方法用于比较两个字符序列的指定区域是否匹配,支持大小写敏感和忽略大小写两种模式。
     *
     * @param cs        源字符序列
     * @param substring 要比较的子字符序列
     * @param length    要比较的长度
     * @return 如果指定区域匹配返回true,否则返回false
     */
    private static boolean regionMatches(final CharSequence cs, final CharSequence substring, final int length) {
        if (cs instanceof String && substring instanceof String csStr) {
            return csStr.regionMatches(true, 0, csStr, 0, length);
        }

        int index1 = 0;
        int index2 = 0;
        int tmpLen = length;

        final int srcLen = cs.length();
        final int otherLen = substring.length();

        if (length < 0) {
            return false;
        }

        if (srcLen < length || otherLen < length) {
            return false;
        }

        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);

            if (c1 == c2) {
                continue;
            }

            if (Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将字符串从源字符集转换为目标字符集
     *
     * @param input       原始字符串
     * @param fromCharset 源字符集
     * @param toCharset   目标字符集
     * @return 转换后的字符串,转换失败返回原字符串
     */
    public static String convertCharset(String input, Charset fromCharset, Charset toCharset) {
        if (isBlank(input)) {
            return input;
        }
        try {
            byte[] bytes = input.getBytes(fromCharset);
            return new String(bytes, toCharset);
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * 使用逗号连接可迭代对象中的元素
     *
     * @param iterable 可迭代对象
     * @return 连接后的字符串
     */
    public static String joinComma(Iterable<?> iterable) {
        return join(iterable, SEPARATOR);
    }

    /**
     * 使用逗号连接数组中的元素
     *
     * @param array 数组
     * @return 连接后的字符串
     */
    public static String joinComma(Object[] array) {
        return join(array, SEPARATOR);
    }
}
