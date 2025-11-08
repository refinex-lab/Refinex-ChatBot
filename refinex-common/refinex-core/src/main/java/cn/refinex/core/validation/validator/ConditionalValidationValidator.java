package cn.refinex.core.validation.validator;

import cn.refinex.core.validation.annotation.ConditionalValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 条件校验器实现
 * <p>
 * 使用 SpEL 表达式引擎进行条件判断，支持缓存以提高性能
 * </p>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
public class ConditionalValidationValidator implements ConstraintValidator<ConditionalValidation, Object> {

    /**
     * SpEL 表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 表达式缓存，键为 SpEL 表达式字符串，值为解析后的表达式对象
     */
    private static final ConcurrentHashMap<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<FieldKey, VarHandle> VAR_HANDLE_CACHE = new ConcurrentHashMap<>();

    /**
     * 校验条件表达式字符串
     */
    private String condition;

    /**
     * 校验字段名
     */
    private String fieldName;

    /**
     * 校验类型
     */
    private ConditionalValidation.ValidationType validationType;

    /**
     * 校验失败时的错误消息
     */
    private String message;

    /**
     * 初始化校验器，从注解中获取校验条件、字段名、校验类型和错误消息
     *
     * @param annotation 校验注解
     */
    @Override
    public void initialize(ConditionalValidation annotation) {
        this.condition = annotation.condition();
        this.fieldName = annotation.field();
        this.validationType = annotation.type();
        this.message = annotation.message();
    }

    /**
     * 校验对象是否符合条件
     *
     * @param object  校验对象
     * @param context 校验上下文
     * @return 如果对象符合条件则返回 true，否则返回 false
     */
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }

        try {
            // 评估条件表达式
            boolean conditionMet = evaluateCondition(object);

            // 如果条件不满足，则跳过校验
            if (!conditionMet) {
                return true;
            }

            // 条件满足，执行字段校验
            Object fieldValue = getFieldValue(object, fieldName);
            boolean isValid = validateField(fieldValue);

            // 如果校验失败，自定义错误消息
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(fieldName)
                        .addConstraintViolation();
            }

            return isValid;
        } catch (Exception e) {
            log.error("条件校验执行失败 - 条件: {}, 字段: {}, 对象: {}", condition, fieldName, object.getClass().getSimpleName(), e);

            // 校验失败时应该返回 false
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("校验执行异常: " + e.getMessage())
                    .addPropertyNode(fieldName)
                    .addConstraintViolation();
            return false;
        }
    }

    /**
     * 评估校验条件表达式
     *
     * @param object 校验对象
     * @return 如果条件满足则返回 true，否则返回 false
     */
    private boolean evaluateCondition(Object object) {
        Expression expression = EXPRESSION_CACHE.computeIfAbsent(
                condition,
                PARSER::parseExpression
        );

        StandardEvaluationContext context = new StandardEvaluationContext(object);
        Boolean result = expression.getValue(context, Boolean.class);

        return Boolean.TRUE.equals(result);
    }

    /**
     * 获取字段值（支持嵌套属性）
     *
     * @param object    校验对象
     * @param fieldName 字段名（支持嵌套，如 user.email）
     * @return 字段值
     * @throws Exception 如果获取字段值失败
     */
    private Object getFieldValue(Object object, String fieldName) throws Exception {
        // 处理嵌套属性，如 user.email
        String[] fieldParts = fieldName.split("\\.");
        Object current = object;

        for (String part : fieldParts) {
            if (current == null) {
                return null;
            }
            current = getFieldValueDirect(current, part);
        }

        return current;
    }

    /**
     * 直接获取字段值
     * <p>
     * 优先使用 VarHandle API（JDK 9+），它是访问字段的现代化方式，
     * 性能更好且更符合模块化系统的要求。如果 VarHandle 创建失败，
     * 则回退到传统反射方式，但会进行安全检查。
     * </p>
     *
     * @param object    校验对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 如果获取字段值失败
     */
    private Object getFieldValueDirect(Object object, String fieldName) throws Exception {
        Class<?> clazz = object.getClass();
        FieldKey key = new FieldKey(clazz, fieldName);

        // 尝试从缓存获取 VarHandle
        VarHandle varHandle = VAR_HANDLE_CACHE.get(key);
        if (varHandle != null) {
            return varHandle.get(object);
        }

        // 查找字段（包括父类）
        Field field = findField(clazz, fieldName);
        if (field == null) {
            throw new NoSuchFieldException("字段 " + fieldName + " 在类 " + clazz.getName() + " 中不存在");
        }

        // 尝试创建 VarHandle（JDK 9+ 的现代化方式）
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    field.getDeclaringClass(),
                    MethodHandles.lookup()
            );
            VarHandle handle = lookup.unreflectVarHandle(field);
            VAR_HANDLE_CACHE.put(key, handle);
            return handle.get(object);
        } catch (Exception e) {
            log.debug("无法创建 VarHandle，回退到反射方式访问字段: {}", fieldName);
            // 回退到传统反射方式
            return getFieldValueViaReflection(object, field);
        }
    }

    /**
     * 通过传统反射方式获取字段值
     * <p>
     * 仅在 VarHandle 不可用时使用。进行了安全检查，避免在严格的模块化环境中失败。
     * </p>
     *
     * @param object 校验对象
     * @param field  字段对象
     * @return 字段值
     * @throws IllegalAccessException 如果获取字段值失败
     */
    @SuppressWarnings("java:S3011")
    private Object getFieldValueViaReflection(Object object, Field field) throws IllegalAccessException {
        // 检查字段是否已经可访问
        if (!field.canAccess(object)) {
            try {
                // 尝试设置访问权限
                field.setAccessible(true);
            } catch (Exception e) {
                // 在严格的模块化环境中可能会失败
                log.warn("无法设置字段 {} 的访问权限，可能受到模块化系统限制", field.getName());
                throw new IllegalAccessException(
                        "无法访问字段 " + field.getName() + "，建议检查模块配置或使用 getter 方法"
                );
            }
        }
        return field.get(object);
    }

    /**
     * 查找字段（包括父类）
     *
     * @param clazz     类对象
     * @param fieldName 字段名
     * @return 字段对象
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 根据校验类型验证字段值是否符合要求
     *
     * @param value 字段值
     * @return 如果字段值符合要求则返回 true，否则返回 false
     */
    private boolean validateField(Object value) {
        return switch (validationType) {
            case NOT_NULL -> value != null;
            case NOT_EMPTY -> value != null && (!(value instanceof String str) || !str.isEmpty());
            case NOT_BLANK -> value != null && (!(value instanceof String str) || !str.isBlank());
            case CUSTOM -> throw new UnsupportedOperationException("CUSTOM 类型需要自定义实现");
        };
    }

    /**
     * 清空表达式缓存（用于测试或重新加载配置）
     */
    public static void clearCache() {
        EXPRESSION_CACHE.clear();
    }

    /**
     * 字段缓存键
     * <p>
     * 用于在缓存中唯一标识一个类的某个字段
     * </p>
     */
    private record FieldKey(Class<?> clazz, String fieldName) {
    }
}
