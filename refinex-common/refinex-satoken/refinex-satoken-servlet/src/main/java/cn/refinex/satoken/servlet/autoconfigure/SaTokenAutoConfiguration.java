package cn.refinex.satoken.servlet.autoconfigure;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 自动配置类
 */
@Slf4j
@AutoConfiguration
public class SaTokenAutoConfiguration implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     * <p>
     * 对所有路由进行 Token 认证校验
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 路由拦截器拦截所有路径，自定义拦截验证规则
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    /**
     * 注册 Sa-Token 全局过滤器 进行 Token 认证校验
     * <p>
     * 内部服务外网隔离: <a href="https://sa-token.cc/doc.html#/micro/same-token?id=%e5%be%ae%e6%9c%8d%e5%8a%a1-%e5%86%85%e9%83%a8%e6%9c%8d%e5%8a%a1%e5%a4%96%e7%bd%91%e9%9a%94%e7%a6%bb">...</a>
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                // 过滤所有路径
                .addInclude("/**")
                // 排除 actuator 路径
                .addExclude("/actuator", "/actuator/**")
                // 排除 favicon.ico 路径
                .addExclude("/favicon.ico")
                // 自定义认证规则
                .setAuth(obj -> {
                    // 检查是否校验 Same-Token (部分 rpc 插件有效)
                    Boolean checkSameToken = SaManager.getConfig().getCheckSameToken();
                    if (Boolean.TRUE.equals(checkSameToken)) {
                        // 校验 Same-Token 身份凭证
                        String token = SaHolder.getRequest().getHeader(SaSameUtil.SAME_TOKEN);
                        SaSameUtil.checkToken(token);
                    }
                })
                // 自定义错误规则
                .setError(e ->
                        SaResult.error("认证失败，无法访问系统资源")
                                .setCode(HttpStatus.UNAUTHORIZED.value())
                );
    }

}
