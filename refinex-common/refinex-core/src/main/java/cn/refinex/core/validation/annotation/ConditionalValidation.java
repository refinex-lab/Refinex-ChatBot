package cn.refinex.core.validation.annotation;

import cn.refinex.core.validation.validator.ConditionalValidationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 条件校验注解（类级别）
 * <p>
 * 根据条件表达式校验指定字段是否满足要求。支持完整的 SpEL 表达式。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @ConditionalValidation(
 *     condition = "loginType == 1",
 *     field = "username",
 *     message = "登录类型为密码登录时，用户名不能为空"
 * )
 * @ConditionalValidation(
 *     condition = "loginType == 2",
 *     field = "email",
 *     message = "登录类型为邮箱登录时，邮箱不能为空"
 * )
 * public class LoginRequest {
 *     private Integer loginType;
 *     private String username;
 *     private String email;
 * }
 * }</pre>
 *
 * @author Refinex
 * @since 2.0.0
 */
@Documented
@Constraint(validatedBy = ConditionalValidationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConditionalValidation.List.class)
public @interface ConditionalValidation {

    /**
     * 条件表达式（SpEL 表达式）
     * <p>
     * 表达式中可以直接使用对象的字段名，如：loginType == 1
     * 支持复杂表达式：loginType == 1 && status != null
     * </p>
     */
    String condition();

    /**
     * 需要校验的字段名
     * <p>
     * 支持嵌套属性，如：user.email
     * </p>
     */
    String field();

    /**
     * 校验类型
     */
    ValidationType type() default ValidationType.NOT_BLANK;

    /**
     * 校验失败时的错误消息
     */
    String message() default "字段校验失败";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 定义多个条件校验
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ConditionalValidation[] value();
    }

    /**
     * 校验类型枚举
     */
    enum ValidationType {
        /**
         * 不能为空（null、空字符串、空白字符串）
         */
        NOT_BLANK,

        /**
         * 不能为 null
         */
        NOT_NULL,

        /**
         * 不能为空（null 或空字符串，但允许空白字符串）
         */
        NOT_EMPTY,

        /**
         * 自定义校验（需要配合 customValidator 使用）
         */
        CUSTOM
    }
}
