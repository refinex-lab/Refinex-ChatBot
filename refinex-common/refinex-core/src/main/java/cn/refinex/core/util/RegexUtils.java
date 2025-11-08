package cn.refinex.core.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正则表达式工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegexUtils {

    /**
     * Pattern 缓存，避免重复编译
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>(64);

    /**
     * 缓存最大容量
     */
    private static final int MAX_CACHE_SIZE = 100;

    // ==================== 预定义正则表达式模式 ====================

    /**
     * 邮箱地址正则表达式
     * 支持常见邮箱格式，包括中文域名
     */
    public static final String EMAIL = "^[a-zA-Z0-9_+.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z0-9]{2,}$";

    /**
     * 中国大陆手机号正则表达式
     * 支持13-19开头的11位手机号
     */
    public static final String MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 中国大陆固定电话正则表达式
     * 格式：区号-电话号码，区号3-4位，电话号码7-8位
     */
    public static final String TELEPHONE = "^0\\d{2,3}-?\\d{7,8}$";

    /**
     * 中国大陆身份证号正则表达式（18位）
     * 包含基本格式校验
     */
    public static final String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$";

    /**
     * 中国大陆邮政编码正则表达式
     * 6位数字
     */
    public static final String ZIP_CODE = "^[1-9]\\d{5}$";

    /**
     * IPv4地址正则表达式
     */
    public static final String IPV4 = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    /**
     * IPv6地址正则表达式（简化版）
     */
    public static final String IPV6 = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";

    /**
     * URL地址正则表达式
     * 支持http、https、ftp协议
     */
    public static final String URL = "^(https?|ftp)://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$";

    /**
     * 用户名正则表达式
     * 4-16位字母、数字、下划线、中文
     */
    public static final String USERNAME = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{4,16}$";

    /**
     * 强密码正则表达式
     * 至少8位，包含大小写字母、数字、特殊字符
     */
    public static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    /**
     * 中等密码正则表达式
     * 至少6位，包含字母和数字
     */
    public static final String MEDIUM_PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$";

    /**
     * 整数正则表达式（包括正负整数和零）
     */
    public static final String INTEGER = "^-?\\d+$";

    /**
     * 正整数正则表达式
     */
    public static final String POSITIVE_INTEGER = "^[1-9]\\d*$";

    /**
     * 非负整数正则表达式（包括零）
     */
    public static final String NON_NEGATIVE_INTEGER = "^\\d+$";

    /**
     * 浮点数正则表达式
     */
    public static final String DECIMAL = "^-?\\d+\\.\\d+$";

    /**
     * 数字正则表达式（整数或浮点数）
     */
    public static final String NUMBER = "^-?\\d+(\\.\\d+)?$";

    /**
     * 中文字符正则表达式
     */
    public static final String CHINESE = "^[\\u4e00-\\u9fa5]+$";

    /**
     * 英文字母正则表达式
     */
    public static final String LETTER = "^[A-Za-z]+$";

    /**
     * 字母和数字正则表达式
     */
    public static final String ALPHANUMERIC = "^[A-Za-z0-9]+$";

    /**
     * 日期格式正则表达式（yyyy-MM-dd）
     */
    public static final String DATE = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

    /**
     * 时间格式正则表达式（HH:mm:ss）
     */
    public static final String TIME = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";

    /**
     * 日期时间格式正则表达式（yyyy-MM-dd HH:mm:ss）
     */
    public static final String DATETIME = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]) ([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";

    /**
     * 银行卡号正则表达式
     * 13-19位数字
     */
    public static final String BANK_CARD = "^[1-9]\\d{12,18}$";

    /**
     * 车牌号正则表达式（普通车牌）
     * 支持新能源车牌
     */
    public static final String CAR_NUMBER = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$";

    /**
     * MAC地址正则表达式
     */
    public static final String MAC_ADDRESS = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

    /**
     * 十六进制颜色代码正则表达式
     */
    public static final String HEX_COLOR = "^#?([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$";

    /**
     * 微信号正则表达式
     * 6-20位字母、数字、下划线、减号，字母开头
     */
    public static final String WECHAT = "^[a-zA-Z][a-zA-Z0-9_-]{5,19}$";

    /**
     * QQ号正则表达式
     * 5-11位数字，不以0开头
     */
    public static final String QQ = "^[1-9][0-9]{4,10}$";

    // ==================== 预定义模式验证方法 ====================

    /**
     * 验证邮箱地址
     *
     * @param email 邮箱地址
     * @return 是否为有效邮箱
     */
    public static boolean isEmail(String email) {
        return matches(email, EMAIL);
    }

    /**
     * 验证手机号码
     *
     * @param mobile 手机号码
     * @return 是否为有效手机号
     */
    public static boolean isMobile(String mobile) {
        return matches(mobile, MOBILE);
    }

    /**
     * 验证固定电话
     *
     * @param telephone 固定电话
     * @return 是否为有效固定电话
     */
    public static boolean isTelephone(String telephone) {
        return matches(telephone, TELEPHONE);
    }

    /**
     * 验证手机号或固定电话
     *
     * @param phone 电话号码
     * @return 是否为有效电话号码
     */
    public static boolean isPhone(String phone) {
        return isMobile(phone) || isTelephone(phone);
    }

    /**
     * 验证身份证号码
     * <p>
     * 包含格式验证和校验码验证。
     * </p>
     *
     * @param idCard 身份证号码
     * @return 是否为有效身份证号
     */
    public static boolean isIdCard(String idCard) {
        if (!matches(idCard, ID_CARD)) {
            return false;
        }
        return validateIdCardChecksum(idCard);
    }

    /**
     * 验证身份证校验码
     *
     * @param idCard 身份证号码
     * @return 校验码是否正确
     */
    private static boolean validateIdCardChecksum(String idCard) {
        try {
            int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
            char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

            int sum = 0;
            for (int i = 0; i < 17; i++) {
                sum += Character.getNumericValue(idCard.charAt(i)) * weights[i];
            }

            char checkCode = checkCodes[sum % 11];
            return idCard.charAt(17) == checkCode || (checkCode == 'X' && idCard.charAt(17) == 'x');
        } catch (Exception e) {
            log.error("身份证校验码验证失败", e);
            return false;
        }
    }

    /**
     * 验证邮政编码
     *
     * @param zipCode 邮政编码
     * @return 是否为有效邮政编码
     */
    public static boolean isZipCode(String zipCode) {
        return matches(zipCode, ZIP_CODE);
    }

    /**
     * 验证IPv4地址
     *
     * @param ipv4 IPv4地址
     * @return 是否为有效IPv4地址
     */
    public static boolean isIpv4(String ipv4) {
        return matches(ipv4, IPV4);
    }

    /**
     * 验证IPv6地址
     *
     * @param ipv6 IPv6地址
     * @return 是否为有效IPv6地址
     */
    public static boolean isIpv6(String ipv6) {
        return matches(ipv6, IPV6);
    }

    /**
     * 验证URL地址
     *
     * @param url URL地址
     * @return 是否为有效URL
     */
    public static boolean isUrl(String url) {
        return matches(url, URL);
    }

    /**
     * 验证用户名
     *
     * @param username 用户名
     * @return 是否为有效用户名
     */
    public static boolean isUsername(String username) {
        return matches(username, USERNAME);
    }

    /**
     * 验证强密码
     *
     * @param password 密码
     * @return 是否为强密码
     */
    public static boolean isStrongPassword(String password) {
        return matches(password, STRONG_PASSWORD);
    }

    /**
     * 验证中等密码
     *
     * @param password 密码
     * @return 是否为中等强度密码
     */
    public static boolean isMediumPassword(String password) {
        return matches(password, MEDIUM_PASSWORD);
    }

    /**
     * 验证整数
     *
     * @param str 字符串
     * @return 是否为整数
     */
    public static boolean isInteger(String str) {
        return matches(str, INTEGER);
    }

    /**
     * 验证正整数
     *
     * @param str 字符串
     * @return 是否为正整数
     */
    public static boolean isPositiveInteger(String str) {
        return matches(str, POSITIVE_INTEGER);
    }

    /**
     * 验证非负整数
     *
     * @param str 字符串
     * @return 是否为非负整数
     */
    public static boolean isNonNegativeInteger(String str) {
        return matches(str, NON_NEGATIVE_INTEGER);
    }

    /**
     * 验证浮点数
     *
     * @param str 字符串
     * @return 是否为浮点数
     */
    public static boolean isDecimal(String str) {
        return matches(str, DECIMAL);
    }

    /**
     * 验证数字（整数或浮点数）
     *
     * @param str 字符串
     * @return 是否为数字
     */
    public static boolean isNumber(String str) {
        return matches(str, NUMBER);
    }

    /**
     * 验证中文字符
     *
     * @param str 字符串
     * @return 是否全为中文字符
     */
    public static boolean isChinese(String str) {
        return matches(str, CHINESE);
    }

    /**
     * 验证英文字母
     *
     * @param str 字符串
     * @return 是否全为英文字母
     */
    public static boolean isLetter(String str) {
        return matches(str, LETTER);
    }

    /**
     * 验证字母和数字
     *
     * @param str 字符串
     * @return 是否全为字母和数字
     */
    public static boolean isAlphanumeric(String str) {
        return matches(str, ALPHANUMERIC);
    }

    /**
     * 验证日期格式（yyyy-MM-dd）
     *
     * @param date 日期字符串
     * @return 是否为有效日期格式
     */
    public static boolean isDate(String date) {
        return matches(date, DATE);
    }

    /**
     * 验证时间格式（HH:mm:ss）
     *
     * @param time 时间字符串
     * @return 是否为有效时间格式
     */
    public static boolean isTime(String time) {
        return matches(time, TIME);
    }

    /**
     * 验证日期时间格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param datetime 日期时间字符串
     * @return 是否为有效日期时间格式
     */
    public static boolean isDateTime(String datetime) {
        return matches(datetime, DATETIME);
    }

    /**
     * 验证银行卡号
     *
     * @param bankCard 银行卡号
     * @return 是否为有效银行卡号
     */
    public static boolean isBankCard(String bankCard) {
        return matches(bankCard, BANK_CARD);
    }

    /**
     * 验证车牌号
     *
     * @param carNumber 车牌号
     * @return 是否为有效车牌号
     */
    public static boolean isCarNumber(String carNumber) {
        return matches(carNumber, CAR_NUMBER);
    }

    /**
     * 验证MAC地址
     *
     * @param macAddress MAC地址
     * @return 是否为有效MAC地址
     */
    public static boolean isMacAddress(String macAddress) {
        return matches(macAddress, MAC_ADDRESS);
    }

    /**
     * 验证十六进制颜色代码
     *
     * @param hexColor 颜色代码
     * @return 是否为有效十六进制颜色代码
     */
    public static boolean isHexColor(String hexColor) {
        return matches(hexColor, HEX_COLOR);
    }

    /**
     * 验证微信号
     *
     * @param wechat 微信号
     * @return 是否为有效微信号
     */
    public static boolean isWechat(String wechat) {
        return matches(wechat, WECHAT);
    }

    /**
     * 验证QQ号
     *
     * @param qq QQ号
     * @return 是否为有效QQ号
     */
    public static boolean isQQ(String qq) {
        return matches(qq, QQ);
    }

    // ==================== 基础正则匹配方法 ====================

    /**
     * 判断字符串是否匹配正则表达式
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 是否匹配
     */
    public static boolean matches(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return false;
        }
        try {
            Pattern pattern = getPattern(regex);
            return pattern.matcher(input).matches();
        } catch (PatternSyntaxException e) {
            log.error("正则表达式语法错误: {}", regex, e);
            return false;
        }
    }

    /**
     * 判断字符串是否包含匹配正则表达式的内容
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 是否包含匹配内容
     */
    public static boolean contains(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return false;
        }
        try {
            Pattern pattern = getPattern(regex);
            return pattern.matcher(input).find();
        } catch (PatternSyntaxException e) {
            log.error("正则表达式语法错误: {}", regex, e);
            return false;
        }
    }

    /**
     * 统计正则表达式在字符串中的匹配次数
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 匹配次数
     */
    public static int count(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return 0;
        }
        try {
            Pattern pattern = getPattern(regex);
            Matcher matcher = pattern.matcher(input);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (PatternSyntaxException e) {
            log.error("正则表达式语法错误: {}", regex, e);
            return 0;
        }
    }

    // ==================== 正则提取方法 ====================

    /**
     * 提取第一个匹配的字符串
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 第一个匹配的字符串，未找到返回null
     */
    public static String extractFirst(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return null;
        }
        try {
            return ReUtil.get(regex, input, 0);
        } catch (Exception e) {
            log.error("提取匹配字符串失败: regex={}, input={}", regex, input, e);
            return null;
        }
    }

    /**
     * 提取第一个匹配的指定分组
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @param group 分组索引
     * @return 匹配的分组内容，未找到返回null
     */
    public static String extractFirstGroup(String input, String regex, int group) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return null;
        }
        try {
            return ReUtil.get(regex, input, group);
        } catch (Exception e) {
            log.error("提取匹配分组失败: regex={}, input={}, group={}", regex, input, group, e);
            return null;
        }
    }

    /**
     * 提取所有匹配的字符串
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 所有匹配的字符串列表
     */
    public static List<String> extractAll(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return Collections.emptyList();
        }
        try {
            return ReUtil.findAll(regex, input, 0);
        } catch (Exception e) {
            log.error("提取所有匹配字符串失败: regex={}, input={}", regex, input, e);
            return Collections.emptyList();
        }
    }

    /**
     * 提取所有匹配的指定分组
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @param group 分组索引
     * @return 所有匹配的分组内容列表
     */
    public static List<String> extractAllGroups(String input, String regex, int group) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return Collections.emptyList();
        }
        try {
            return ReUtil.findAll(regex, input, group);
        } catch (Exception e) {
            log.error("提取所有匹配分组失败: regex={}, input={}, group={}", regex, input, group, e);
            return Collections.emptyList();
        }
    }

    /**
     * 提取所有匹配结果的所有分组
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 所有匹配结果的分组列表，每个元素是一个匹配结果的所有分组
     */
    public static List<List<String>> extractAllWithGroups(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return Collections.emptyList();
        }
        try {
            Pattern pattern = getPattern(regex);
            Matcher matcher = pattern.matcher(input);
            List<List<String>> results = new ArrayList<>();

            while (matcher.find()) {
                List<String> groups = new ArrayList<>();
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    groups.add(matcher.group(i));
                }
                results.add(groups);
            }
            return results;
        } catch (Exception e) {
            log.error("提取所有匹配分组失败: regex={}, input={}", regex, input, e);
            return Collections.emptyList();
        }
    }

    // ==================== 正则替换方法 ====================

    /**
     * 替换第一个匹配的内容
     *
     * @param input       输入字符串
     * @param regex       正则表达式
     * @param replacement 替换内容
     * @return 替换后的字符串
     */
    public static String replaceFirst(String input, String regex, String replacement) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return input;
        }
        if (replacement == null) {
            replacement = "";
        }
        try {
            String finalReplacement = replacement;
            return ReUtil.replaceAll(input, regex, matcher -> {
                if (matcher.start() == 0 || !matcher.find(0)) {
                    return finalReplacement;
                }
                return matcher.group();
            });
        } catch (Exception e) {
            log.error("替换匹配内容失败: regex={}, input={}", regex, input, e);
            return input;
        }
    }

    /**
     * 替换所有匹配的内容
     *
     * @param input       输入字符串
     * @param regex       正则表达式
     * @param replacement 替换内容
     * @return 替换后的字符串
     */
    public static String replaceAll(String input, String regex, String replacement) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return input;
        }
        if (replacement == null) {
            replacement = "";
        }
        try {
            Pattern pattern = getPattern(regex);
            return pattern.matcher(input).replaceAll(replacement);
        } catch (Exception e) {
            log.error("替换所有匹配内容失败: regex={}, input={}", regex, input, e);
            return input;
        }
    }

    /**
     * 删除第一个匹配的内容
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 删除后的字符串
     */
    public static String removeFirst(String input, String regex) {
        return replaceFirst(input, regex, "");
    }

    /**
     * 删除所有匹配的内容
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 删除后的字符串
     */
    public static String removeAll(String input, String regex) {
        return replaceAll(input, regex, "");
    }

    // ==================== 正则分割方法 ====================

    /**
     * 根据正则表达式分割字符串
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @return 分割后的字符串数组
     */
    public static String[] split(String input, String regex) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return new String[]{input};
        }
        try {
            Pattern pattern = getPattern(regex);
            return pattern.split(input);
        } catch (Exception e) {
            log.error("分割字符串失败: regex={}, input={}", regex, input, e);
            return new String[]{input};
        }
    }

    /**
     * 根据正则表达式分割字符串，限制分割次数
     *
     * @param input 输入字符串
     * @param regex 正则表达式
     * @param limit 分割次数限制
     * @return 分割后的字符串数组
     */
    public static String[] split(String input, String regex, int limit) {
        if (StrUtil.isBlank(input) || StrUtil.isBlank(regex)) {
            return new String[]{input};
        }
        try {
            Pattern pattern = getPattern(regex);
            return pattern.split(input, limit);
        } catch (Exception e) {
            log.error("分割字符串失败: regex={}, input={}, limit={}", regex, input, limit, e);
            return new String[]{input};
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 转义正则表达式特殊字符
     * <p>
     * 将字符串中的正则特殊字符进行转义，使其可作为普通字符串匹配。
     * </p>
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escape(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }
        return Pattern.quote(input);
    }

    /**
     * 获取Pattern对象（带缓存）
     * <p>
     * 从缓存中获取已编译的Pattern，如果不存在则编译并缓存。
     * </p>
     *
     * @param regex 正则表达式
     * @return Pattern对象
     */
    private static Pattern getPattern(String regex) {
        return getPattern(regex, 0);
    }

    /**
     * 获取Pattern对象（带缓存和标志位）
     *
     * @param regex 正则表达式
     * @param flags 标志位
     * @return Pattern对象
     */
    private static synchronized Pattern getPattern(String regex, int flags) {
        String key = regex + "_" + flags;
        Pattern pattern = PATTERN_CACHE.get(key);

        if (pattern == null) {
            // 检查缓存大小，防止内存溢出
            if (PATTERN_CACHE.size() >= MAX_CACHE_SIZE) {
                // 清除最早的一半缓存
                Iterator<String> iterator = PATTERN_CACHE.keySet().iterator();
                int removeCount = MAX_CACHE_SIZE / 2;
                for (int i = 0; i < removeCount && iterator.hasNext(); i++) {
                    iterator.next();
                    iterator.remove();
                }
                log.debug("Pattern缓存已满，清除{}个最早的缓存", removeCount);
            }

            pattern = Pattern.compile(regex, flags);
            PATTERN_CACHE.put(key, pattern);
        }

        return pattern;
    }

    /**
     * 清除Pattern缓存
     */
    public static synchronized void clearPatternCache() {
        PATTERN_CACHE.clear();
        log.info("Pattern缓存已清除");
    }

    /**
     * 获取Pattern缓存大小
     *
     * @return 缓存大小
     */
    public static int getPatternCacheSize() {
        return PATTERN_CACHE.size();
    }

    /**
     * 验证正则表达式语法是否正确
     *
     * @param regex 正则表达式
     * @return 语法是否正确
     */
    public static boolean isValidRegex(String regex) {
        if (StrUtil.isBlank(regex)) {
            return false;
        }
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            log.debug("正则表达式语法错误: {}", regex, e);
            return false;
        }
    }

    /**
     * 手机号脱敏
     * <p>
     * 格式：138****8888
     * </p>
     *
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizeMobile(String mobile) {
        if (!isMobile(mobile)) {
            return mobile;
        }
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏
     * <p>
     * 格式：110101********1234
     * </p>
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (!matches(idCard, ID_CARD)) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
    }

    /**
     * 邮箱脱敏
     * <p>
     * 格式：abc***@example.com
     * </p>
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (!isEmail(email)) {
            return email;
        }
        return email.replaceAll("(\\w{1,3})\\w*@", "$1***@");
    }

    /**
     * 银行卡号脱敏
     * <p>
     * 格式：6222 **** **** 1234
     * </p>
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankCard(String bankCard) {
        if (!isBankCard(bankCard)) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d*(\\d{4})", "$1 **** **** $2");
    }

    /**
     * 自定义脱敏
     * <p>
     * 格式：根据正则表达式替换为指定字符
     * </p>
     *
     * @param input       输入字符串
     * @param regex       正则表达式
     * @param replacement 替换字符
     * @return 脱敏后的字符串
     */
    public static String desensitizeCustom(String input, String regex, String replacement) {
        if (StrUtil.isBlank(input)) {
            return input;
        }
        return input.replaceAll(regex, replacement);
    }

    /**
     * 自定义脱敏（保留首尾字符）
     * <p>
     * 格式：保留前keepFront个字符和后keepEnd个字符，中间用mask替换
     * </p>
     *
     * @param input     输入字符串
     * @param keepFront 保留前端字符数
     * @param keepEnd   保留后端字符数
     * @param mask      替换字符
     * @return 脱敏后的字符串
     */
    public static String desensitizeCustom(String input, int keepFront, int keepEnd, String mask) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        int length = input.length();
        if (length <= keepFront + keepEnd) {
            return input;
        }

        String front = input.substring(0, keepFront);
        String end = input.substring(length - keepEnd);
        String middle = StrUtil.repeat(mask, length - keepFront - keepEnd);
        return front + middle + end;
    }

    /**
     * 姓名脱敏
     * <p>
     * 两个字：*明<br>
     * 三个字及以上：张*明
     * </p>
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        int length = name.length();
        if (length == 1) {
            return "*";
        } else if (length == 2) {
            return "*" + name.charAt(1);
        } else {
            return name.charAt(0) + "*" + name.charAt(length - 1);
        }
    }

    /**
     * 提取所有数字
     *
     * @param input 输入字符串
     * @return 所有数字组成的字符串
     */
    public static String extractNumbers(String input) {
        if (StrUtil.isBlank(input)) {
            return "";
        }
        return String.join("", extractAll(input, "\\d+"));
    }

    /**
     * 提取所有字母
     *
     * @param input 输入字符串
     * @return 所有字母组成的字符串
     */
    public static String extractLetters(String input) {
        if (StrUtil.isBlank(input)) {
            return "";
        }
        return String.join("", extractAll(input, "[a-zA-Z]+"));
    }

    /**
     * 提取所有中文字符
     *
     * @param input 输入字符串
     * @return 所有中文字符组成的字符串
     */
    public static String extractChinese(String input) {
        if (StrUtil.isBlank(input)) {
            return "";
        }
        return String.join("", extractAll(input, "[\\u4e00-\\u9fa5]+"));
    }

    /**
     * 提取所有邮箱地址
     *
     * @param input 输入字符串
     * @return 所有邮箱地址列表
     */
    public static List<String> extractEmails(String input) {
        if (StrUtil.isBlank(input)) {
            return Collections.emptyList();
        }
        return extractAll(input, EMAIL);
    }

    /**
     * 提取所有URL
     *
     * @param input 输入字符串
     * @return 所有URL列表
     */
    public static List<String> extractUrls(String input) {
        if (StrUtil.isBlank(input)) {
            return Collections.emptyList();
        }
        return extractAll(input, URL);
    }

    /**
     * 提取所有手机号
     *
     * @param input 输入字符串
     * @return 所有手机号列表
     */
    public static List<String> extractMobiles(String input) {
        if (StrUtil.isBlank(input)) {
            return Collections.emptyList();
        }
        return extractAll(input, MOBILE);
    }

    /**
     * 提取所有IP地址（IPv4）
     *
     * @param input 输入字符串
     * @return 所有IP地址列表
     */
    public static List<String> extractIpv4s(String input) {
        if (StrUtil.isBlank(input)) {
            return Collections.emptyList();
        }
        return extractAll(input, IPV4);
    }

    /**
     * 判断字符串是否包含中文
     *
     * @param input 输入字符串
     * @return 是否包含中文
     */
    public static boolean containsChinese(String input) {
        return contains(input, "[\\u4e00-\\u9fa5]");
    }

    /**
     * 判断字符串是否包含表情符号
     *
     * @param input 输入字符串
     * @return 是否包含表情符号
     */
    public static boolean containsEmoji(String input) {
        if (StrUtil.isBlank(input)) {
            return false;
        }
        String emojiRegex = "[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]";
        return contains(input, emojiRegex);
    }

    /**
     * 移除所有空白字符
     *
     * @param input 输入字符串
     * @return 移除空白字符后的字符串
     */
    public static String removeWhitespace(String input) {
        return removeAll(input, "\\s+");
    }

    /**
     * 移除所有HTML标签
     *
     * @param input 输入字符串
     * @return 移除HTML标签后的字符串
     */
    public static String removeHtmlTags(String input) {
        return removeAll(input, "<[^>]+>");
    }

    /**
     * 移除所有特殊字符（保留字母、数字、中文、空格）
     *
     * @param input 输入字符串
     * @return 移除特殊字符后的字符串
     */
    public static String removeSpecialChars(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }
        return input.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]", "");
    }

    /**
     * 验证密码强度
     * <p>
     * 返回密码强度等级：0-弱，1-中，2-强
     * </p>
     *
     * @param password 密码
     * @return 密码强度等级（0/1/2）
     */
    public static int getPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }

        int strength = 0;

        // 长度检查
        if (password.length() >= 8) {
            strength++;
        }

        // 包含小写字母
        if (contains(password, "[a-z]")) {
            strength++;
        }

        // 包含大写字母
        if (contains(password, "[A-Z]")) {
            strength++;
        }

        // 包含数字
        if (contains(password, "\\d")) {
            strength++;
        }

        // 包含特殊字符
        if (contains(password, "[^a-zA-Z0-9]")) {
            strength++;
        }

        // 根据满足条件数量判断强度
        if (strength <= 2) {
            // 弱
            return 0;
        } else if (strength <= 3) {
            // 中
            return 1;
        } else {
            // 强
            return 2;
        }
    }

    /**
     * 批量验证
     * <p>
     * 验证多个字符串是否都匹配正则表达式。
     * </p>
     *
     * @param regex  正则表达式
     * @param inputs 输入字符串数组
     * @return 是否全部匹配
     */
    public static boolean matchesAll(String regex, String... inputs) {
        if (StrUtil.isBlank(regex) || inputs == null || inputs.length == 0) {
            return false;
        }
        return Arrays.stream(inputs).allMatch(input -> matches(input, regex));
    }

    /**
     * 批量验证（任一匹配）
     * <p>
     * 验证多个字符串中是否有任一匹配正则表达式。
     * </p>
     *
     * @param regex  正则表达式
     * @param inputs 输入字符串数组
     * @return 是否有任一匹配
     */
    public static boolean matchesAny(String regex, String... inputs) {
        if (StrUtil.isBlank(regex) || inputs == null || inputs.length == 0) {
            return false;
        }
        return Arrays.stream(inputs).anyMatch(input -> matches(input, regex));
    }

}
