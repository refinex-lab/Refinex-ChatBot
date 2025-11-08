package cn.refinex.core.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.http.HttpHeaders;

/**
 * 设备识别工具类
 * <p>
 * 基于 Yauaa 库解析 User-Agent，识别设备类型、浏览器、操作系统等信息
 * </p>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeviceUtils {

    /**
     * User-Agent 解析器（单例，线程安全）
     */
    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = UserAgentAnalyzer
            .newBuilder()
            // 缓存 10000 个解析结果
            .withCache(10000)
            .hideMatcherLoadStats()
            .build();

    /**
     * 设备类型常量
     */
    public static final String DEVICE_TYPE_PC = "PC";
    public static final String DEVICE_TYPE_APP = "APP";
    public static final String DEVICE_TYPE_H5 = "H5";
    public static final String DEVICE_TYPE_UNKNOWN = "UNKNOWN";

    /**
     * 从 HttpServletRequest 中获取设备类型
     * <p>
     * 优先使用前端传递的 deviceType 参数，降级使用 User-Agent 解析
     * </p>
     *
     * @param request          HttpServletRequest 对象
     * @param clientDeviceType 前端传递的设备类型（可选）
     * @return 设备类型（PC、APP、H5、UNKNOWN）
     */
    public static String getDeviceType(HttpServletRequest request, String clientDeviceType) {
        // 优先使用前端传递的设备类型
        if (StrUtil.isNotBlank(clientDeviceType)) {
            String upperDeviceType = clientDeviceType.toUpperCase();
            if (DEVICE_TYPE_PC.equals(upperDeviceType)
                    || DEVICE_TYPE_APP.equals(upperDeviceType)
                    || DEVICE_TYPE_H5.equals(upperDeviceType)) {
                return upperDeviceType;
            }
        }

        // 降级方案：解析 User-Agent
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        return parseDeviceType(userAgent);
    }

    /**
     * 从 HttpServletRequest 中获取设备类型（默认使用当前请求）
     *
     * @param clientDeviceType 前端传递的设备类型（可选）
     * @return 设备类型（PC、APP、H5、UNKNOWN）
     */
    public static String getDeviceType(String clientDeviceType) {
        return getDeviceType(ServletUtils.getRequest(), clientDeviceType);
    }

    /**
     * 从 User-Agent 字符串解析设备类型
     *
     * @param userAgentString User-Agent 字符串
     * @return 设备类型（PC、APP、H5、UNKNOWN）
     */
    public static String parseDeviceType(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return DEVICE_TYPE_UNKNOWN;
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);

            // 根据设备类别判断
            return switch (deviceClass) {
                case "Desktop", "Unknown" -> DEVICE_TYPE_PC;
                case "Phone", "Tablet" -> {
                    // 进一步判断是 APP 还是 H5
                    String agentName = userAgent.getValue(UserAgent.AGENT_NAME);
                    // 如果包含常见浏览器名称，判定为 H5
                    if (agentName.contains("Chrome") || agentName.contains("Safari") || agentName.contains("Firefox") || agentName.contains("Edge")) {
                        yield DEVICE_TYPE_H5;
                    }
                    // 否则判定为 APP（通常 APP 会自定义 User-Agent）
                    yield DEVICE_TYPE_APP;
                }
                default -> DEVICE_TYPE_UNKNOWN;
            };
        } catch (Exception e) {
            log.warn("解析 User-Agent 失败: {}", userAgentString, e);
            return DEVICE_TYPE_UNKNOWN;
        }
    }

    /**
     * 获取浏览器名称
     *
     * @param userAgentString User-Agent 字符串
     * @return 浏览器名称
     */
    public static String getBrowserName(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.AGENT_NAME);
        } catch (Exception e) {
            log.warn("解析浏览器名称失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 获取浏览器版本
     *
     * @param userAgentString User-Agent 字符串
     * @return 浏览器版本
     */
    public static String getBrowserVersion(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.AGENT_VERSION);
        } catch (Exception e) {
            log.warn("解析浏览器版本失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 获取操作系统名称
     *
     * @param userAgentString User-Agent 字符串
     * @return 操作系统名称
     */
    public static String getOperatingSystemName(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME);
        } catch (Exception e) {
            log.warn("解析操作系统名称失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 获取操作系统版本
     *
     * @param userAgentString User-Agent 字符串
     * @return 操作系统版本
     */
    public static String getOperatingSystemVersion(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.OPERATING_SYSTEM_VERSION);
        } catch (Exception e) {
            log.warn("解析操作系统版本失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 获取设备名称
     *
     * @param userAgentString User-Agent 字符串
     * @return 设备名称
     */
    public static String getDeviceName(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.DEVICE_NAME);
        } catch (Exception e) {
            log.warn("解析设备名称失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 获取设备品牌
     *
     * @param userAgentString User-Agent 字符串
     * @return 设备品牌
     */
    public static String getDeviceBrand(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return "Unknown";
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            return userAgent.getValue(UserAgent.DEVICE_BRAND);
        } catch (Exception e) {
            log.warn("解析设备品牌失败: {}", userAgentString, e);
            return "Unknown";
        }
    }

    /**
     * 判断是否为移动设备
     *
     * @param userAgentString User-Agent 字符串
     * @return true=移动设备，false=非移动设备
     */
    public static boolean isMobileDevice(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return false;
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
            return "Phone".equals(deviceClass) || "Tablet".equals(deviceClass);
        } catch (Exception e) {
            log.warn("判断是否为移动设备失败: {}", userAgentString, e);
            return false;
        }
    }

    /**
     * 判断是否为平板设备
     *
     * @param userAgentString User-Agent 字符串
     * @return true=平板设备，false=非平板设备
     */
    public static boolean isTablet(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return false;
        }

        try {
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
            return "Tablet".equals(deviceClass);
        } catch (Exception e) {
            log.warn("判断是否为平板设备失败: {}", userAgentString, e);
            return false;
        }
    }
}

