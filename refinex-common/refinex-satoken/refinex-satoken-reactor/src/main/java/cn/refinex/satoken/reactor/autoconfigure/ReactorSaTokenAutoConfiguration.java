package cn.refinex.satoken.reactor.autoconfigure;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.refinex.redis.autoconfigure.RefinexRedisAutoConfiguration;
import cn.refinex.satoken.reactor.properties.SaTokenWhiteProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Sa-Token Reactor 自动配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = RefinexRedisAutoConfiguration.class)
@EnableConfigurationProperties(SaTokenWhiteProperties.class)
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
        List<String> whitelistPaths = whiteProperties.getWhites();
        if (CollectionUtils.isNotEmpty(whitelistPaths)) {
            log.debug("Sa-Token 网关白名单路径：{}", whitelistPaths);
            whitelistPaths.forEach(filter::addExclude);
        }

        // 认证函数：每次请求执行
        filter.setAuth(obj -> {
            log.info("Sa-Token 网关鉴权 URL：{}", SaHolder.getRequest().getUrl());

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
}
