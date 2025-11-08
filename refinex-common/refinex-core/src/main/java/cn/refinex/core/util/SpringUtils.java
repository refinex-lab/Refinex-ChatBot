package cn.refinex.core.util;

import cn.hutool.extra.spring.SpringUtil;

import java.util.Objects;

/**
 * Spring 工具类
 *
 * @author 芋道源码
 * @since 1.0.0
 */
public class SpringUtils extends SpringUtil {

    /**
     * 是否是生产环境
     *
     * @return 是否是生产环境
     */
    public static boolean isProd() {
        String activeProfile = getActiveProfile();
        return Objects.equals("prod", activeProfile);
    }
}
