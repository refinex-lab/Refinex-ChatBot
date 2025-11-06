package cn.refinex.core.util;

import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.refinex.core.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址工具类
 * <p>
 * 提供IP地址相关的验证、解析和地理位置查询功能,支持IPv4和IPv6地址。
 * 使用ip2region离线IP地址库进行地理位置查询。
 *
 * @author refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpAddressUtils extends NetUtil {

    /**
     * 未知IP标识
     */
    public static final String UNKNOWN_IP = "XX XX";

    /**
     * 内网地址标识
     */
    public static final String LOCAL_ADDRESS = "内网IP";

    /**
     * 未知地址标识
     */
    public static final String UNKNOWN_ADDRESS = "未知";

    /**
     * IP地址库文件名称
     */
    private static final String IP_XDB_FILENAME = "ip2region.xdb";

    /**
     * IP地址查询器
     */
    private static final Searcher SEARCHER;

    static {
        try {
            SEARCHER = Searcher.newWithBuffer(Version.IPv4, new LongByteArray(ResourceUtil.readBytes(IP_XDB_FILENAME)));
            log.info("IP地址工具初始化成功，IP数据库已加载");
        } catch (NoResourceException e) {
            throw new BusinessException("IpAddressUtils 初始化失败：未找到 IP 数据库文件");
        } catch (Exception e) {
            throw new BusinessException("IP地址工具初始化失败: " + e.getMessage());
        }
    }

    /**
     * 根据IP地址获取真实地理位置
     * <p>
     * 自动识别IPv4和IPv6地址类型:
     * 1. 对于内网地址返回内网IP标识
     * 2. 对于公网地址查询其地理位置信息
     *
     * @param ip IP地址字符串
     * @return 地理位置信息,可能返回内网IP、地理位置或未知标识
     */
    public static String getRealAddressByIp(String ip) {
        String cleanedIp = HtmlUtil.cleanHtmlTag(StrUtil.blankToDefault(ip, ""));

        if (isIPv4(cleanedIp)) {
            return resolveIPv4Region(cleanedIp);
        }

        if (isIPv6(cleanedIp)) {
            return resolveIPv6Region(cleanedIp);
        }

        return UNKNOWN_IP;
    }

    /**
     * 解析IPv4地址的地理位置
     *
     * @param ip IPv4地址
     * @return 地理位置信息
     */
    private static String resolveIPv4Region(String ip) {
        if (isInnerIP(ip)) {
            return LOCAL_ADDRESS;
        }
        return getCityInfo(ip);
    }

    /**
     * 解析IPv6地址的地理位置
     * <p>
     * 当前ip2region不支持IPv6地址解析,对于IPv6地址仅判断是否为内网地址。
     *
     * @param ip IPv6地址
     * @return 地理位置信息
     */
    private static String resolveIPv6Region(String ip) {
        if (isInnerIPv6(ip)) {
            return LOCAL_ADDRESS;
        }
        log.warn("ip2region does not support IPv6 address resolution: {}", ip);
        return UNKNOWN_ADDRESS;
    }

    /**
     * 根据IP地址查询城市信息
     * <p>
     * 使用离线IP地址库查询IP的地理位置信息。
     *
     * @param ip IP地址
     * @return 城市信息,查询失败返回未知标识
     */
    private static String getCityInfo(String ip) {
        try {
            String region = SEARCHER.search(StrUtil.trim(ip));
            return region.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            log.error("Failed to query city info for IP: {}", ip, e);
            return UNKNOWN_ADDRESS;
        }
    }

    /**
     * 判断是否为IPv4地址
     *
     * @param ip IP地址字符串
     * @return 如果是有效的IPv4地址返回true,否则返回false
     */
    public static boolean isIPv4(String ip) {
        return ReUtil.isMatch(PatternPool.IPV4, ip);
    }

    /**
     * 判断是否为IPv6地址
     *
     * @param ip IP地址字符串
     * @return 如果是有效的IPv6地址返回true,否则返回false
     */
    public static boolean isIPv6(String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断IPv6地址是否为内网地址
     * <p>
     * 以下类型的IPv6地址被视为内网地址:
     * <ul>
     *   <li>通配符地址: 0:0:0:0:0:0:0:0</li>
     *   <li>链路本地地址: fe80::/10</li>
     *   <li>唯一本地地址: fec0::/10</li>
     *   <li>环回地址: ::1</li>
     * </ul>
     *
     * @param ip IPv6地址字符串
     * @return 如果是内网地址返回true,否则返回false
     * @throws IllegalArgumentException 如果IP地址格式无效
     */
    public static boolean isInnerIPv6(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address instanceof Inet6Address inet6Address) {
                return inet6Address.isAnyLocalAddress()
                        || inet6Address.isLinkLocalAddress()
                        || inet6Address.isLoopbackAddress()
                        || inet6Address.isSiteLocalAddress();
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv6 address: " + ip, e);
        }
        return false;
    }
}
