package cn.refinex.core.logging.user;

import cn.hutool.core.convert.Convert;
import org.springframework.util.ClassUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 兼容 Sa-Token 的用户解析器 (反射调用 LoginHelper)
 *
 * @author Refinex
 * @since 1.0.0
 */
public final class RequestUserExtractor {

    /**
     * Sa-Token 的 LoginHelper 类全名
     */
    private static final String LOGIN_HELPER_CLASS = "cn.refinex.satoken.common.helper.LoginHelper";

    /**
     * Sa-Token 的 LoginUser 类全名
     */
    private static final String LOGIN_USER_CLASS = "cn.refinex.satoken.common.model.LoginUser";

    /**
     * 是否已完成初始化 (无论成功与否)
     */
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * 反射调用 LoginHelper.getLoginUserOrNull() 方法的 MethodHandle
     */
    private static MethodHandle loginUserMethod;

    /**
     * 反射调用 LoginUser.getUserId() 方法的 MethodHandle
     */
    private static MethodHandle getUserIdMethod;

    /**
     * 反射调用 LoginUser.getUsername() 方法的 MethodHandle
     */
    private static MethodHandle getUsernameMethod;

    /**
     * 私有构造函数，防止实例化
     */
    private RequestUserExtractor() {
        throw new UnsupportedOperationException("RequestUserExtractor is a utility class and cannot be instantiated");
    }

    /**
     * 获取当前登录用户 (若无法解析则返回 empty)
     *
     * @return 当前登录用户
     */
    public static Optional<RequestUser> currentUser() {
        if (!initialize()) {
            return Optional.empty();
        }

        try {
            Object loginUser = loginUserMethod.invoke();
            if (Objects.isNull(loginUser)) {
                return Optional.empty();
            }

            Long userId = Convert.toLong(getUserIdMethod.invoke(loginUser));
            String username = Convert.toStr(getUsernameMethod.invoke(loginUser));
            return Optional.of(new RequestUser(userId, username));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * 初始化 MethodHandle, 若已初始化则直接返回是否成功
     *
     * @return 是否初始化成功
     */
    private static boolean initialize() {
        if (INITIALIZED.get()) {
            return loginUserMethod != null;
        }

        // 双重检查锁定，确保线程安全且只初始化一次
        synchronized (RequestUserExtractor.class) {
            if (INITIALIZED.get()) {
                return loginUserMethod != null;
            }
            if (!ClassUtils.isPresent(LOGIN_HELPER_CLASS, RequestUserExtractor.class.getClassLoader())) {
                INITIALIZED.set(true);
                return false;
            }
            try {
                ClassLoader cl = RequestUserExtractor.class.getClassLoader();
                Class<?> helperClass = ClassUtils.forName(LOGIN_HELPER_CLASS, cl);
                Class<?> loginUserClass = ClassUtils.forName(LOGIN_USER_CLASS, cl);
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                loginUserMethod = lookup.findStatic(helperClass, "getLoginUserOrNull", MethodType.methodType(loginUserClass));
                getUserIdMethod = lookup.findVirtual(loginUserClass, "getUserId", MethodType.methodType(Long.class));
                getUsernameMethod = lookup.findVirtual(loginUserClass, "getUsername", MethodType.methodType(String.class));
                INITIALIZED.set(true);
                return true;
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                INITIALIZED.set(true);
                return false;
            }
        }
    }
}
