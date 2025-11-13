package cn.refinex.core.logging.annotation;

import cn.refinex.core.logging.enums.RequestLogType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Controller 请求日志注解
 *
 * @author Refinex
 * @since 1.0.0
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface RequestLog {

    /**
     * 日志标题
     */
    String title() default "";

    /**
     * 业务类型
     */
    RequestLogType type() default RequestLogType.OTHER;

    /**
     * 额外描述
     */
    String description() default "";

    /**
     * 是否记录请求体
     */
    boolean recordRequestBody() default true;

    /**
     * 是否记录响应体
     */
    boolean recordResponseBody() default false;

    /**
     * 是否执行持久化
     */
    boolean persist() default true;
}
