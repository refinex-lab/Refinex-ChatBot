package cn.refinex.satoken.reactor.autoconfigure;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.refinex.core.constants.HttpServicesGroupConstants;
import cn.refinex.redis.autoconfigure.RefinexRedisAutoConfiguration;
import cn.refinex.satoken.reactor.properties.SaTokenWhiteProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Sa-Token Reactor 自动配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = RefinexRedisAutoConfiguration.class)
@EnableConfigurationProperties({SaTokenWhiteProperties.class})
public class ReactorSaTokenAutoConfiguration {

    /**
     * 注册 Sa-Token 全局过滤器进行鉴权操作
     *
     * @return SaReactorFilter 实例
     */
    @Bean
    public SaReactorFilter saReactorFilter(SaTokenWhiteProperties whiteProperties) {
        // 拦截所有路径
        SaReactorFilter filter = new SaReactorFilter().addInclude("/**");

        // 添加白名单路径
        List<String> servicePrefixes = resolveServicePrefixes();
        List<String> whitelistPaths = expandWhitelist(whiteProperties.getWhites(), servicePrefixes);
        if (CollectionUtils.isNotEmpty(whitelistPaths)) {
            log.debug("Sa-Token 网关白名单路径：{}", whitelistPaths);
            whitelistPaths.forEach(filter::addExclude);
        }

        // 认证函数：每次请求执行
        filter.setAuth(obj -> {
            String url = SaHolder.getRequest().getUrl();
            log.info("Sa-Token 网关鉴权 URL：{}", url);

            // 使用 SaRouter 进行路由级别的权限控制, 不在白名单路径中的请求均需要登录
            SaRouter.match("/**")
                    .notMatch(whitelistPaths)
                    .check(r -> StpUtil.checkLogin());
        });

        // 异常处理函数：每次 setAuth 函数出现异常时进入
        filter.setError(e -> {
            log.error("Sa-Token 网关鉴权异常：{}", e.getMessage());

            // 如果是未登录异常, 则返回未登录错信息
            if (e instanceof NotLoginException) {
                return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED.value());
            }
            // 其他异常, 返回通用错误信息
            return SaResult.error("认证失败，无法访问系统资源").setCode(HttpStatus.UNAUTHORIZED.value());
        });

        return filter;
    }

    /**
     * 扩展白名单，兼容带服务前缀和去前缀两种请求路径形式。
     * 例如：/refinex-platform/captcha 和 /captcha 均加入白名单，避免 StripPrefix 顺序导致的不匹配。
     */
    private static List<String> expandWhitelist(List<String> src, List<String> prefixes) {
        if (CollectionUtils.isEmpty(src)) {
            return src;
        }
        return src.stream()
                .filter(Objects::nonNull)
                .flatMap(p -> {
                    String path = p.trim();
                    // 原始
                    // 去前缀
                    List<String> stripped = prefixes.stream()
                            .filter(path::startsWith)
                            .map(prefix -> path.substring(prefix.length()).trim())
                            .filter(s -> s.startsWith("/"))
                            .toList();
                    // 加前缀
                    List<String> withPrefixes = prefixes.stream()
                            .map(prefix -> prefix + (path.startsWith("/") ? path : ("/" + path)))
                            .toList();
                    return Stream.concat(Stream.of(path), Stream.concat(stripped.stream(), withPrefixes.stream()));
                })
                .distinct()
                .toList();
    }

    /**
     * 解析服务前缀列表：
     * - 优先使用配置项 gateway.service.prefixes
     * - 如果未配置，使用常量 HttpServicesGroupConstants 中的服务名自动拼接为 "/{service}"
     */
    private static List<String> resolveServicePrefixes() {
        List<String> list = HttpServicesGroupConstants.ALL_GROUPS;
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("/") ? s : ("/" + s))
                .distinct()
                .toList();
    }
}
