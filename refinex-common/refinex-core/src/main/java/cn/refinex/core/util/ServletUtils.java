package cn.refinex.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Servlet 工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServletUtils {

    /**
     * 默认字符编码
     */
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    /**
     * JSON响应内容类型
     */
    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;

    /**
     * 文本响应内容类型
     */
    private static final String CONTENT_TYPE_TEXT = MediaType.TEXT_PLAIN_VALUE;

    /**
     * HTML响应内容类型
     */
    private static final String CONTENT_TYPE_HTML = MediaType.TEXT_HTML_VALUE;

    /**
     * XML响应内容类型
     */
    private static final String CONTENT_TYPE_XML = MediaType.APPLICATION_XML_VALUE;

    /**
     * 文件下载内容类型
     */
    private static final String CONTENT_TYPE_DOWNLOAD = MediaType.APPLICATION_OCTET_STREAM_VALUE;

    /**
     * 未知IP标识
     */
    private static final String UNKNOWN = "unknown";

    /**
     * 本地IP地址
     */
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * IP地址最大长度
     */
    private static final int IP_MAX_LENGTH = 15;

    /**
     * 代理IP分隔符
     */
    private static final String IP_SEPARATOR = ",";

    // ==================== 请求对象获取 ====================

    /**
     * 获取当前请求的 HttpServletRequest 对象
     *
     * @return 当前请求的 HttpServletRequest 对象
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    // ==================== 请求参数获取 ====================

    /**
     * 获取请求参数（String类型）
     * <p>
     * 如果参数不存在或为空，返回null。
     * </p>
     *
     * @param request   HttpServletRequest对象
     * @param paramName 参数名称
     * @return 参数值
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static String getParameter(HttpServletRequest request, String paramName) {
        validateRequest(request);
        validateNotBlank(paramName, "参数名称不能为空");
        String value = request.getParameter(paramName);
        return StrUtil.isBlank(value) ? null : value.trim();
    }

    /**
     * 获取请求参数（带默认值）
     *
     * @param request      HttpServletRequest对象
     * @param paramName    参数名称
     * @param defaultValue 默认值
     * @return 参数值，不存在时返回默认值
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String value = getParameter(request, paramName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取请求参数（Integer类型）
     *
     * @param request   HttpServletRequest对象
     * @param paramName 参数名称
     * @return 参数值，解析失败返回null
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Integer getParameterAsInt(HttpServletRequest request, String paramName) {
        String value = getParameter(request, paramName);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("参数{}转换为Integer失败: {}", paramName, value);
            return null;
        }
    }

    /**
     * 获取请求参数（Integer类型，带默认值）
     *
     * @param request      HttpServletRequest对象
     * @param paramName    参数名称
     * @param defaultValue 默认值
     * @return 参数值，解析失败返回默认值
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Integer getParameterAsInt(HttpServletRequest request, String paramName, Integer defaultValue) {
        Integer value = getParameterAsInt(request, paramName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取请求参数（Long类型）
     *
     * @param request   HttpServletRequest对象
     * @param paramName 参数名称
     * @return 参数值，解析失败返回null
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Long getParameterAsLong(HttpServletRequest request, String paramName) {
        String value = getParameter(request, paramName);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("参数{}转换为Long失败: {}", paramName, value);
            return null;
        }
    }

    /**
     * 获取请求参数（Long类型，带默认值）
     *
     * @param request      HttpServletRequest对象
     * @param paramName    参数名称
     * @param defaultValue 默认值
     * @return 参数值，解析失败返回默认值
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Long getParameterAsLong(HttpServletRequest request, String paramName, Long defaultValue) {
        Long value = getParameterAsLong(request, paramName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取请求参数（Boolean类型）
     * <p>
     * 支持true/false、yes/no、1/0、on/off等格式。
     * </p>
     *
     * @param request   HttpServletRequest对象
     * @param paramName 参数名称
     * @return 参数值，解析失败返回null
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Boolean getParameterAsBoolean(HttpServletRequest request, String paramName) {
        String value = getParameter(request, paramName);
        if (value == null) {
            return null;
        }
        return "true".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value)
                || "1".equals(value)
                || "on".equalsIgnoreCase(value);
    }

    /**
     * 获取请求参数（Boolean类型，带默认值）
     *
     * @param request      HttpServletRequest对象
     * @param paramName    参数名称
     * @param defaultValue 默认值
     * @return 参数值，解析失败返回默认值
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static Boolean getParameterAsBoolean(HttpServletRequest request, String paramName, Boolean defaultValue) {
        Boolean value = getParameterAsBoolean(request, paramName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取所有请求参数
     *
     * @param request HttpServletRequest对象
     * @return 参数Map，键为参数名，值为参数值数组
     * @throws IllegalArgumentException 如果request为null
     */
    public static Map<String, String[]> getParameterMap(HttpServletRequest request) {
        validateRequest(request);
        return new HashMap<>(request.getParameterMap());
    }

    /**
     * 获取所有请求参数（扁平化）
     *
     * @param request HttpServletRequest对象
     * @return 参数Map，键为参数名，值为参数值
     * @throws IllegalArgumentException 如果request为null
     */
    public static Map<String, String> getParameterMapFlat(HttpServletRequest request) {
        validateRequest(request);
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> result = HashMap.newHashMap(paramMap.size());

        paramMap.forEach((key, values) -> {
            if (values != null && values.length > 0 && StrUtil.isNotBlank(values[0])) {
                result.put(key, ArrayUtil.join(values, StrUtil.COMMA));
            }
        });

        return result;
    }

    /**
     * 获取请求参数数组
     *
     * @param request   HttpServletRequest对象
     * @param paramName 参数名称
     * @return 参数值数组
     * @throws IllegalArgumentException 如果request或参数名为null
     */
    public static String[] getParameterValues(HttpServletRequest request, String paramName) {
        validateRequest(request);
        validateNotBlank(paramName, "参数名称不能为空");
        return request.getParameterValues(paramName);
    }

    // ==================== 请求头操作 ====================

    /**
     * 获取请求头
     *
     * @param request    HttpServletRequest对象
     * @param headerName 请求头名称
     * @return 请求头值
     * @throws IllegalArgumentException 如果request或请求头名称为null
     */
    public static String getHeader(HttpServletRequest request, String headerName) {
        validateRequest(request);
        validateNotBlank(headerName, "请求头名称不能为空");
        String value = request.getHeader(headerName);
        return StrUtil.isBlank(value) ? null : value.trim();
    }

    /**
     * 获取请求头（带默认值）
     *
     * @param request      HttpServletRequest对象
     * @param headerName   请求头名称
     * @param defaultValue 默认值
     * @return 请求头值，不存在时返回默认值
     * @throws IllegalArgumentException 如果request或请求头名称为null
     */
    public static String getHeader(HttpServletRequest request, String headerName, String defaultValue) {
        String value = getHeader(request, headerName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取所有请求头
     *
     * @param request HttpServletRequest对象
     * @return 请求头Map
     * @throws IllegalArgumentException 如果request为null
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        validateRequest(request);
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (StrUtil.isNotBlank(headerValue)) {
                headerMap.put(headerName, headerValue.trim());
            }
        }

        return headerMap;
    }

    /**
     * 获取User-Agent
     *
     * @param request HttpServletRequest对象
     * @return User-Agent值
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getUserAgent(HttpServletRequest request) {
        return getHeader(request, "User-Agent");
    }

    /**
     * 获取当前请求的User-Agent
     *
     * @return User-Agent值
     */
    public static String getUserAgent() {
        return getUserAgent(getRequest());
    }

    /**
     * 获取Referer
     *
     * @param request HttpServletRequest对象
     * @return Referer值
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getReferer(HttpServletRequest request) {
        return getHeader(request, "Referer");
    }

    /**
     * 获取Content-Type
     *
     * @param request HttpServletRequest对象
     * @return Content-Type值
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getContentType(HttpServletRequest request) {
        validateRequest(request);
        return request.getContentType();
    }

    // ==================== 请求体读取 ====================

    /**
     * 读取请求体内容（String格式）
     * <p>
     * 适用于读取POST请求的JSON、XML等文本数据。
     * 注意：请求体只能读取一次，读取后会关闭流。
     * </p>
     *
     * @param request HttpServletRequest对象
     * @return 请求体内容
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRequestBody(HttpServletRequest request) {
        validateRequest(request);

        // 仅 JSON 请求才读取，因为只有 JSON 请求 CacheRequestBodyFilter 才会进行缓存以支持重复读取
        if (!isJsonRequest(request)) {
            return null;
        }

        return JakartaServletUtil.getBody(request);
    }

    /**
     * 读取请求体内容（byte数组格式）
     *
     * @param request HttpServletRequest对象
     * @return 请求体字节数组
     * @throws IllegalArgumentException 如果request为null
     */
    public static byte[] getRequestBodyBytes(HttpServletRequest request) {
        validateRequest(request);
        // 仅 JSON 请求才读取，因为只有 JSON 请求 CacheRequestBodyFilter 才会进行缓存以支持重复读取
        if (!isJsonRequest(request)) {
            return new byte[0];
        }

        return JakartaServletUtil.getBodyBytes(request);
    }

    /**
     * 判断是否为JSON请求
     *
     * @param request HttpServletRequest对象
     * @return 是否为JSON请求, true为JSON请求, false为非JSON请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        validateRequest(request);
        return StrUtil.startWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 读取JSON请求体并转换为对象
     *
     * @param request HttpServletRequest对象
     * @param clazz   目标类型
     * @param <T>     对象类型
     * @return 转换后的对象，解析失败返回null
     * @throws IllegalArgumentException 如果request或目标类型为null
     */
    public static <T> T getJsonBody(HttpServletRequest request, Class<T> clazz) {
        validateRequest(request);
        Objects.requireNonNull(clazz, "目标类型不能为null");

        try {
            String body = getRequestBody(request);
            if (StrUtil.isBlank(body)) {
                return null;
            }
            return JSONUtil.toBean(body, clazz);
        } catch (Exception e) {
            log.error("解析JSON请求体失败", e);
            return null;
        }
    }

    // ==================== 请求信息获取 ====================

    /**
     * 获取请求方法
     *
     * @param request HttpServletRequest对象
     * @return 请求方法（GET、POST等）
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getMethod(HttpServletRequest request) {
        validateRequest(request);
        return request.getMethod();
    }

    /**
     * 判断是否为GET请求
     *
     * @param request HttpServletRequest对象
     * @return 是否为GET请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(getMethod(request));
    }

    /**
     * 判断是否为POST请求
     *
     * @param request HttpServletRequest对象
     * @return 是否为POST请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(getMethod(request));
    }

    /**
     * 判断是否为Ajax请求
     * <p>
     * 通过X-Requested-With请求头判断。
     * </p>
     *
     * @param request HttpServletRequest对象
     * @return 是否为Ajax请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = getHeader(request, "X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
    }

    /**
     * 获取请求URI
     *
     * @param request HttpServletRequest对象
     * @return 请求URI
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRequestUri(HttpServletRequest request) {
        validateRequest(request);
        return request.getRequestURI();
    }

    /**
     * 获取请求URL
     *
     * @param request HttpServletRequest对象
     * @return 请求URL
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRequestUrl(HttpServletRequest request) {
        validateRequest(request);
        return request.getRequestURL().toString();
    }

    /**
     * 获取完整请求URL（包含查询参数）
     *
     * @param request HttpServletRequest对象
     * @return 完整请求URL
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        validateRequest(request);
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (StrUtil.isNotBlank(queryString)) {
            url += "?" + queryString;
        }
        return url;
    }

    /**
     * 获取查询参数字符串
     *
     * @param request HttpServletRequest对象
     * @return 查询参数字符串
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getQueryString(HttpServletRequest request) {
        validateRequest(request);
        return request.getQueryString();
    }

    /**
     * 获取上下文路径
     *
     * @param request HttpServletRequest对象
     * @return 上下文路径
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getContextPath(HttpServletRequest request) {
        validateRequest(request);
        return request.getContextPath();
    }

    /**
     * 获取Servlet路径
     *
     * @param request HttpServletRequest对象
     * @return Servlet路径
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getServletPath(HttpServletRequest request) {
        validateRequest(request);
        return request.getServletPath();
    }

    // ==================== 客户端IP获取 ====================

    /**
     * 获取当前请求的客户端IP地址
     * <p>
     * 调用{@link #getClientIp(HttpServletRequest)}方法，传入当前请求对象。
     * </p>
     *
     * @return 客户端IP地址
     * @throws IllegalArgumentException 如果当前请求对象为null
     */
    public static String getClientIp() {
        return getClientIp(getRequest());
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 支持通过代理服务器和负载均衡器获取真实IP。
     * 按以下顺序尝试获取：X-Forwarded-For、X-Real-IP、Proxy-Client-IP、
     * WL-Proxy-Client-IP、HTTP_CLIENT_IP、HTTP_X_FORWARDED_FOR、RemoteAddr。
     * </p>
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getClientIp(HttpServletRequest request) {
        validateRequest(request);

        String ip = getHeader(request, "X-Forwarded-For");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        ip = getHeader(request, "X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getRemoteAddr();
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip;
    }

    /**
     * 验证IP地址是否有效
     *
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip)
                && !UNKNOWN.equalsIgnoreCase(ip)
                && ip.length() <= IP_MAX_LENGTH;
    }

    /**
     * 从多个IP中获取第一个有效IP
     * <p>
     * 处理X-Forwarded-For中可能存在的多个IP地址。
     * </p>
     *
     * @param ips IP地址字符串（可能包含多个IP，用逗号分隔）
     * @return 第一个有效IP
     */
    private static String getFirstIp(String ips) {
        if (StrUtil.isBlank(ips)) {
            return ips;
        }
        String[] ipArray = ips.split(IP_SEPARATOR);
        for (String ip : ipArray) {
            ip = ip.trim();
            if (isValidIp(ip) && !isInternalIp(ip)) {
                return ip;
            }
        }
        return ipArray[0].trim();
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    private static boolean isInternalIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }

        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }

        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            return isInternalIp(addr);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断字节数组表示的IP是否为内网IP
     *
     * @param addr IP地址字节数组
     * @return 是否为内网IP
     */
    private static boolean isInternalIp(byte[] addr) {
        if (addr == null || addr.length < 2) {
            return false;
        }

        final byte b0 = addr[0];
        final byte b1 = addr[1];

        // 10.x.x.x
        final byte section1 = 0x0A;
        // 172.16.x.x ~ 172.31.x.x
        final byte section2 = (byte) 0xAC;
        final byte section3 = (byte) 0x10;
        final byte section4 = (byte) 0x1F;
        // 192.168.x.x
        final byte section5 = (byte) 0xC0;
        final byte section6 = (byte) 0xA8;

        switch (b0) {
            case section1:
                return true;
            case section2:
                if (b1 >= section3 && b1 <= section4) {
                    return true;
                }
                break;
            case section5:
                if (b1 == section6) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * 获取客户端主机名
     *
     * @param request HttpServletRequest对象
     * @return 客户端主机名
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getClientHostname(HttpServletRequest request) {
        validateRequest(request);
        String ip = getClientIp(request);
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getHostName();
        } catch (UnknownHostException e) {
            log.warn("无法解析主机名: {}", ip);
            return ip;
        }
    }

    // ==================== 响应输出 ====================

    /**
     * 输出JSON响应
     *
     * @param response HttpServletResponse对象
     * @param object   要输出的对象
     * @return 是否输出成功
     * @throws IllegalArgumentException 如果response为null
     */
    public static boolean writeJson(HttpServletResponse response, Object object) {
        validateResponse(response);
        try {
            response.setContentType(CONTENT_TYPE_JSON);
            response.setCharacterEncoding(DEFAULT_ENCODING);
            String json = JSONUtil.toJsonStr(object);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("输出JSON响应失败", e);
            return false;
        }
    }

    /**
     * 输出文本响应
     *
     * @param response HttpServletResponse对象
     * @param text     文本内容
     * @return 是否输出成功
     * @throws IllegalArgumentException 如果response为null
     */
    public static boolean writeText(HttpServletResponse response, String text) {
        validateResponse(response);
        try {
            response.setContentType(CONTENT_TYPE_TEXT);
            response.setCharacterEncoding(DEFAULT_ENCODING);
            PrintWriter writer = response.getWriter();
            writer.write(text != null ? text : "");
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("输出文本响应失败", e);
            return false;
        }
    }

    /**
     * 输出HTML响应
     *
     * @param response HttpServletResponse对象
     * @param html     HTML内容
     * @return 是否输出成功
     * @throws IllegalArgumentException 如果response为null
     */
    public static boolean writeHtml(HttpServletResponse response, String html) {
        validateResponse(response);
        try {
            response.setContentType(CONTENT_TYPE_HTML);
            response.setCharacterEncoding(DEFAULT_ENCODING);
            PrintWriter writer = response.getWriter();
            writer.write(html != null ? html : "");
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("输出HTML响应失败", e);
            return false;
        }
    }

    /**
     * 输出XML响应
     *
     * @param response HttpServletResponse对象
     * @param xml      XML内容
     * @return 是否输出成功
     * @throws IllegalArgumentException 如果response为null
     */
    public static boolean writeXml(HttpServletResponse response, String xml) {
        validateResponse(response);
        try {
            response.setContentType(CONTENT_TYPE_XML);
            response.setCharacterEncoding(DEFAULT_ENCODING);
            PrintWriter writer = response.getWriter();
            writer.write(xml != null ? xml : "");
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("输出XML响应失败", e);
            return false;
        }
    }

    /**
     * 设置文件下载响应头
     *
     * @param response HttpServletResponse对象
     * @param fileName 文件名
     * @throws IllegalArgumentException 如果response或文件名为null
     */
    public static void setDownloadHeader(HttpServletResponse response, String fileName) {
        validateResponse(response);
        validateNotBlank(fileName, "文件名不能为空");

        try {
            String encodedFileName = URLUtil.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType(CONTENT_TYPE_DOWNLOAD);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + encodedFileName);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        } catch (Exception e) {
            log.error("设置文件下载响应头失败: {}", fileName, e);
        }
    }

    /**
     * 输出文件下载
     *
     * @param response HttpServletResponse对象
     * @param fileName 文件名
     * @param data     文件数据
     * @return 是否输出成功
     * @throws IllegalArgumentException 如果response、文件名或数据为null
     */
    public static boolean downloadFile(HttpServletResponse response, String fileName, byte[] data) {
        validateResponse(response);
        validateNotBlank(fileName, "文件名不能为空");
        Objects.requireNonNull(data, "文件数据不能为null");

        try {
            setDownloadHeader(response, fileName);
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
            return true;
        } catch (IOException e) {
            log.error("输出文件下载失败: {}", fileName, e);
            return false;
        }
    }

    /**
     * 设置响应状态码
     *
     * @param response   HttpServletResponse对象
     * @param statusCode HTTP状态码
     * @throws IllegalArgumentException 如果response为null
     */
    public static void setStatus(HttpServletResponse response, int statusCode) {
        validateResponse(response);
        response.setStatus(statusCode);
    }

    /**
     * 设置响应头
     *
     * @param response    HttpServletResponse对象
     * @param headerName  响应头名称
     * @param headerValue 响应头值
     * @throws IllegalArgumentException 如果response或响应头名称为null
     */
    public static void setHeader(HttpServletResponse response, String headerName, String headerValue) {
        validateResponse(response);
        validateNotBlank(headerName, "响应头名称不能为空");
        response.setHeader(headerName, headerValue);
    }

    /**
     * 设置缓存控制响应头
     *
     * @param response HttpServletResponse对象
     * @param maxAge   最大缓存时间（秒）
     * @throws IllegalArgumentException 如果response为null
     */
    public static void setCacheControl(HttpServletResponse response, int maxAge) {
        validateResponse(response);
        response.setHeader("Cache-Control", "max-age=" + maxAge);
    }

    /**
     * 设置禁用缓存
     *
     * @param response HttpServletResponse对象
     * @throws IllegalArgumentException 如果response为null
     */
    public static void setNoCache(HttpServletResponse response) {
        validateResponse(response);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    // ==================== Cookie操作 ====================

    /**
     * 获取Cookie值
     *
     * @param request    HttpServletRequest对象
     * @param cookieName Cookie名称
     * @return Cookie值，不存在返回null
     * @throws IllegalArgumentException 如果request或Cookie名称为null
     */
    public static String getCookie(HttpServletRequest request, String cookieName) {
        validateRequest(request);
        validateNotBlank(cookieName, "Cookie名称不能为空");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 获取Cookie对象
     *
     * @param request    HttpServletRequest对象
     * @param cookieName Cookie名称
     * @return Cookie对象，不存在返回null
     * @throws IllegalArgumentException 如果request或Cookie名称为null
     */
    public static Cookie getCookieObject(HttpServletRequest request, String cookieName) {
        validateRequest(request);
        validateNotBlank(cookieName, "Cookie名称不能为空");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 获取所有Cookie
     *
     * @param request HttpServletRequest对象
     * @return Cookie列表
     * @throws IllegalArgumentException 如果request为null
     */
    public static List<Cookie> getAllCookies(HttpServletRequest request) {
        validateRequest(request);
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(cookies);
    }

    /**
     * 添加Cookie
     *
     * @param response HttpServletResponse对象
     * @param name     Cookie名称
     * @param value    Cookie值
     * @param maxAge   有效期（秒），-1表示浏览器关闭时失效
     * @throws IllegalArgumentException 如果response或Cookie名称为null
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        addCookie(response, name, value, null, maxAge, false, false);
    }

    /**
     * 添加Cookie（完整参数）
     *
     * @param response HttpServletResponse对象
     * @param name     Cookie名称
     * @param value    Cookie值
     * @param path     Cookie路径
     * @param maxAge   有效期（秒），-1表示浏览器关闭时失效
     * @param httpOnly 是否仅HTTP访问
     * @param secure   是否仅HTTPS传输
     * @throws IllegalArgumentException 如果response或Cookie名称为null
     */
    public static void addCookie(HttpServletResponse response, String name, String value, String path, int maxAge, boolean httpOnly, boolean secure) {
        validateResponse(response);
        validateNotBlank(name, "Cookie名称不能为空");

        Cookie cookie = new Cookie(name, value != null ? value : "");
        cookie.setPath(StrUtil.isNotBlank(path) ? path : "/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    /**
     * 删除Cookie
     *
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param name     Cookie名称
     * @throws IllegalArgumentException 如果request、response或Cookie名称为null
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        deleteCookie(request, response, name, null);
    }

    /**
     * 删除Cookie（指定路径）
     *
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param name     Cookie名称
     * @param path     Cookie路径
     * @throws IllegalArgumentException 如果request、response或Cookie名称为null
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name, String path) {
        validateRequest(request);
        validateResponse(response);
        validateNotBlank(name, "Cookie名称不能为空");

        Cookie cookie = getCookieObject(request, name);
        if (cookie != null) {
            cookie.setValue("");
            cookie.setMaxAge(0);
            cookie.setPath(StrUtil.isNotBlank(path) ? path : "/");
            response.addCookie(cookie);
        }
    }

    // ==================== Session操作 ====================

    /**
     * 获取Session对象
     *
     * @param request HttpServletRequest对象
     * @return Session对象
     * @throws IllegalArgumentException 如果request为null
     */
    public static HttpSession getSession(HttpServletRequest request) {
        return getSession(request, true);
    }

    /**
     * 获取Session对象
     *
     * @param request HttpServletRequest对象
     * @param create  如果不存在是否创建
     * @return Session对象
     * @throws IllegalArgumentException 如果request为null
     */
    public static HttpSession getSession(HttpServletRequest request, boolean create) {
        validateRequest(request);
        return request.getSession(create);
    }

    /**
     * 获取Session属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @param <T>           属性类型
     * @return 属性值，不存在返回null
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSessionAttribute(HttpServletRequest request, String attributeName) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");

        HttpSession session = getSession(request, false);
        if (session == null) {
            return null;
        }
        return (T) session.getAttribute(attributeName);
    }

    /**
     * 设置Session属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @param value         属性值
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    public static void setSessionAttribute(HttpServletRequest request, String attributeName, Object value) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");

        HttpSession session = getSession(request, true);
        session.setAttribute(attributeName, value);
    }

    /**
     * 移除Session属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    public static void removeSessionAttribute(HttpServletRequest request, String attributeName) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");

        HttpSession session = getSession(request, false);
        if (session != null) {
            session.removeAttribute(attributeName);
        }
    }

    /**
     * 获取所有Session属性名
     *
     * @param request HttpServletRequest对象
     * @return 属性名列表
     * @throws IllegalArgumentException 如果request为null
     */
    public static List<String> getSessionAttributeNames(HttpServletRequest request) {
        validateRequest(request);
        HttpSession session = getSession(request, false);
        if (session == null) {
            return Collections.emptyList();
        }

        List<String> attributeNames = new ArrayList<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            attributeNames.add(names.nextElement());
        }
        return attributeNames;
    }

    /**
     * 清空Session所有属性
     *
     * @param request HttpServletRequest对象
     * @throws IllegalArgumentException 如果request为null
     */
    public static void clearSession(HttpServletRequest request) {
        validateRequest(request);
        HttpSession session = getSession(request, false);
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                session.removeAttribute(names.nextElement());
            }
        }
    }

    /**
     * 使Session失效
     *
     * @param request HttpServletRequest对象
     * @throws IllegalArgumentException 如果request为null
     */
    public static void invalidateSession(HttpServletRequest request) {
        validateRequest(request);
        HttpSession session = getSession(request, false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                log.warn("Session已失效", e);
            }
        }
    }

    /**
     * 获取Session ID
     *
     * @param request HttpServletRequest对象
     * @return Session ID
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getSessionId(HttpServletRequest request) {
        validateRequest(request);
        HttpSession session = getSession(request, false);
        return session != null ? session.getId() : null;
    }

    /**
     * 设置Session最大不活动时间
     *
     * @param request             HttpServletRequest对象
     * @param maxInactiveInterval 最大不活动时间（秒）
     * @throws IllegalArgumentException 如果request为null
     */
    public static void setSessionMaxInactiveInterval(HttpServletRequest request, int maxInactiveInterval) {
        validateRequest(request);
        HttpSession session = getSession(request, true);
        session.setMaxInactiveInterval(maxInactiveInterval);
    }

    // ==================== 请求属性操作 ====================

    /**
     * 获取请求属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @param <T>           属性类型
     * @return 属性值，不存在返回null
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRequestAttribute(HttpServletRequest request, String attributeName) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");
        return (T) request.getAttribute(attributeName);
    }

    /**
     * 设置请求属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @param value         属性值
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    public static void setRequestAttribute(HttpServletRequest request, String attributeName, Object value) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");
        request.setAttribute(attributeName, value);
    }

    /**
     * 移除请求属性
     *
     * @param request       HttpServletRequest对象
     * @param attributeName 属性名称
     * @throws IllegalArgumentException 如果request或属性名称为null
     */
    public static void removeRequestAttribute(HttpServletRequest request, String attributeName) {
        validateRequest(request);
        validateNotBlank(attributeName, "属性名称不能为空");
        request.removeAttribute(attributeName);
    }

    // ==================== 跨域配置 ====================

    /**
     * 设置CORS跨域响应头（允许所有域）
     *
     * @param response HttpServletResponse对象
     * @throws IllegalArgumentException 如果response为null
     */
    public static void setCorsHeaders(HttpServletResponse response) {
        setCorsHeaders(response, "*", "GET, POST, PUT, DELETE, OPTIONS", "Content-Type, Authorization", true);
    }

    /**
     * 设置CORS跨域响应头（自定义）
     *
     * @param response         HttpServletResponse对象
     * @param allowOrigin      允许的来源
     * @param allowMethods     允许的方法
     * @param allowHeaders     允许的请求头
     * @param allowCredentials 是否允许携带凭证
     * @throws IllegalArgumentException 如果response为null
     */
    public static void setCorsHeaders(HttpServletResponse response, String allowOrigin, String allowMethods, String allowHeaders, boolean allowCredentials) {
        validateResponse(response);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin != null ? allowOrigin : "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, allowMethods != null ? allowMethods : "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders != null ? allowHeaders : "*");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(allowCredentials));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }

    // ==================== 重定向与转发 ====================

    /**
     * 重定向到指定URL
     *
     * @param response HttpServletResponse对象
     * @param url      目标URL
     * @return 是否重定向成功
     * @throws IllegalArgumentException 如果response或URL为null
     */
    public static boolean redirect(HttpServletResponse response, String url) {
        validateResponse(response);
        validateNotBlank(url, "重定向URL不能为空");

        try {
            response.sendRedirect(url);
            return true;
        } catch (IOException e) {
            log.error("重定向失败: {}", url, e);
            return false;
        }
    }

    /**
     * 转发到指定路径
     *
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param path     目标路径
     * @return 是否转发成功
     * @throws IllegalArgumentException 如果request、response或路径为null
     */
    public static boolean forward(HttpServletRequest request, HttpServletResponse response, String path) {
        validateRequest(request);
        validateResponse(response);
        validateNotBlank(path, "转发路径不能为空");

        try {
            request.getRequestDispatcher(path).forward(request, response);
            return true;
        } catch (Exception e) {
            log.error("转发失败: {}", path, e);
            return false;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取基础URL
     * <p>
     * 格式：协议://主机:端口/上下文路径
     * 例如：<a href="http://localhost:8080/myapp">...</a>
     * </p>
     *
     * @param request HttpServletRequest对象
     * @return 基础URL
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getBaseUrl(HttpServletRequest request) {
        validateRequest(request);
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(request.getScheme()).append("://")
                .append(request.getServerName());

        int port = request.getServerPort();
        if ((port != 80 && "http".equals(request.getScheme()))
                || (port != 443 && "https".equals(request.getScheme()))) {
            baseUrl.append(":").append(port);
        }

        String contextPath = request.getContextPath();
        if (StrUtil.isNotBlank(contextPath)) {
            baseUrl.append(contextPath);
        }

        return baseUrl.toString();
    }

    /**
     * 判断是否为多部分请求（文件上传）
     *
     * @param request HttpServletRequest对象
     * @return 是否为多部分请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        validateRequest(request);
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 获取请求协议
     *
     * @param request HttpServletRequest对象
     * @return 请求协议（http或https）
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getScheme(HttpServletRequest request) {
        validateRequest(request);
        return request.getScheme();
    }

    /**
     * 判断是否为HTTPS请求
     *
     * @param request HttpServletRequest对象
     * @return 是否为HTTPS请求
     * @throws IllegalArgumentException 如果request为null
     */
    public static boolean isHttps(HttpServletRequest request) {
        return "https".equalsIgnoreCase(getScheme(request));
    }

    /**
     * 获取服务器名称
     *
     * @param request HttpServletRequest对象
     * @return 服务器名称
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getServerName(HttpServletRequest request) {
        validateRequest(request);
        return request.getServerName();
    }

    /**
     * 获取服务器端口
     *
     * @param request HttpServletRequest对象
     * @return 服务器端口
     * @throws IllegalArgumentException 如果request为null
     */
    public static int getServerPort(HttpServletRequest request) {
        validateRequest(request);
        return request.getServerPort();
    }

    /**
     * 获取远程地址
     *
     * @param request HttpServletRequest对象
     * @return 远程地址
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        validateRequest(request);
        return request.getRemoteAddr();
    }

    /**
     * 获取远程主机
     *
     * @param request HttpServletRequest对象
     * @return 远程主机
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRemoteHost(HttpServletRequest request) {
        validateRequest(request);
        return request.getRemoteHost();
    }

    /**
     * 获取远程端口
     *
     * @param request HttpServletRequest对象
     * @return 远程端口
     * @throws IllegalArgumentException 如果request为null
     */
    public static int getRemotePort(HttpServletRequest request) {
        validateRequest(request);
        return request.getRemotePort();
    }

    /**
     * 获取请求的完整信息（用于日志记录）
     *
     * @param request HttpServletRequest对象
     * @return 请求信息字符串
     * @throws IllegalArgumentException 如果request为null
     */
    public static String getRequestInfo(HttpServletRequest request) {
        validateRequest(request);
        return "请求方法: " + getMethod(request) + "\n" +
                "请求URL: " + getFullRequestUrl(request) + "\n" +
                "客户端IP: " + getClientIp(request) + "\n" +
                "User-Agent: " + getUserAgent(request) + "\n" +
                "Content-Type: " + getContentType(request) + "\n" +
                "参数: " + getParameterMapFlat(request) + "\n";
    }

    /**
     * 验证Request对象
     *
     * @param request HttpServletRequest对象
     * @throws IllegalArgumentException 如果request为null
     */
    private static void validateRequest(HttpServletRequest request) {
        Objects.requireNonNull(request, "HttpServletRequest不能为null");
    }

    /**
     * 验证Response对象
     *
     * @param response HttpServletResponse对象
     * @throws IllegalArgumentException 如果response为null
     */
    private static void validateResponse(HttpServletResponse response) {
        Objects.requireNonNull(response, "HttpServletResponse不能为null");
    }

    /**
     * 验证字符串不为空
     *
     * @param str     字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为空
     */
    private static void validateNotBlank(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }
}
