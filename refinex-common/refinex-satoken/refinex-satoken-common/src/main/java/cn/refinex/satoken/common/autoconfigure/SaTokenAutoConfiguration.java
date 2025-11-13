package cn.refinex.satoken.common.autoconfigure;

import cn.refinex.satoken.common.exception.handler.SaTokenExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Sa-Token 自动配置类
 */
@Slf4j
@AutoConfiguration
public class SaTokenAutoConfiguration {

    /**
     * 注册 Sa-Token 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        log.info("Sa-Token 异常处理器注册成功");
        return new SaTokenExceptionHandler();
    }

}
