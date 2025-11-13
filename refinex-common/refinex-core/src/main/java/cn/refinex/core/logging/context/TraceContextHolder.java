package cn.refinex.core.logging.context;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Trace 上下文持有器
 *
 * @author Refinex
 * @since 1.0.0
 */
public final class TraceContextHolder {

    /**
     * Trace 上下文，使用 InheritableThreadLocal 以支持子线程继承父线程的 Trace 上下文
     */
    private static final InheritableThreadLocal<TraceContext> CONTEXT = new InheritableThreadLocal<>();

    /**
     * 私有构造函数，防止外部实例化
     */
    private TraceContextHolder() {
        throw new UnsupportedOperationException("TraceContextHolder 不能被实例化");
    }

    /**
     * 设置 Trace 上下文
     *
     * @param context Trace 上下文
     */
    public static void set(TraceContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前 Trace 上下文的 Optional 包装
     *
     * @return 当前 Trace 上下文的 Optional 包装
     */
    public static Optional<TraceContext> getOptional() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * 获取当前 Trace 上下文
     *
     * @return 当前 Trace 上下文
     */
    @Nullable
    public static TraceContext get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前 Trace 上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前 Trace ID
     *
     * @return 当前 Trace ID
     */
    @Nullable
    public static String currentTraceId() {
        return getOptional().map(TraceContext::traceId).orElse(null);
    }

    /**
     * 获取当前数据签名
     *
     * @return 当前数据签名
     */
    @Nullable
    public static String currentDataSign() {
        return getOptional().map(TraceContext::dataSign).orElse(null);
    }
}
