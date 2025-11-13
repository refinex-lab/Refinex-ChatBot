package cn.refinex.satoken.common.helper;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.core.util.StringUtils;
import cn.refinex.satoken.common.constant.SaTokenConstants;
import cn.refinex.satoken.common.model.LoginUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.refinex.satoken.common.constant.SuperAdminConstants.SUPER_ADMIN_ID;

/**
 * 登录辅助类
 * <p>
 * 提供便捷的登录相关操作方法,封装 Sa-Token 的常用功能。
 * 所有微服务都可以通过此类快速获取登录用户信息、执行登录登出等操作。
 * <p>
 * 核心功能:
 * 1. 用户登录/登出
 * 2. 获取当前登录用户信息
 * 3. 获取用户角色和权限
 * 4. 更新用户Session信息
 * 5. 踢人下线
 * 6. 扩展信息存储和获取
 * <p>
 * 设计说明:
 * - 基于 Sa-Token 的 StpUtil 进行二次封装
 * - 统一了 Session 中 LoginUser 的存储位置
 * - 提供了丰富的便捷方法,简化业务代码
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    /**
     * 用户登录
     * <p>
     * 执行登录操作,生成 Token 并将用户信息存储到 Session 中。
     * 此方法会自动设置登录时间和登录IP。
     *
     * @param loginUser 登录用户信息
     * @param device    登录设备类型(可选,如: PC, MOBILE)
     */
    public static void login(LoginUser loginUser, String device) {
        SaLoginParameter parameter = SaLoginParameter.create();
        if (StringUtils.isNotBlank(device)) {
            parameter.setDeviceType(device);
        }
        login(loginUser, parameter);
    }

    /**
     * 用户登录(无设备类型)
     *
     * @param loginUser 登录用户信息
     */
    public static void login(LoginUser loginUser) {
        login(loginUser, (SaLoginParameter) null);
    }

    /**
     * 用户登录(自定义参数)
     *
     * @param loginUser 登录用户信息
     * @param parameter 登录参数
     */
    public static void login(LoginUser loginUser, SaLoginParameter parameter) {
        // 设置登录时间
        loginUser.setLoginTime(LocalDateTime.now());

        // 执行登录
        if (parameter != null) {
            StpUtil.login(loginUser.getUserId(), parameter);
        } else {
            StpUtil.login(loginUser.getUserId());
        }

        // 存储登录用户信息到 Session
        setLoginUser(loginUser);

        log.debug("用户登录成功: userId={}, username={}, parameter={}", loginUser.getUserId(), loginUser.getUsername(), parameter);
    }

    /**
     * 用户登出
     * <p>
     * 执行登出操作,清除 Token 和 Session 信息。
     */
    public static void logout() {
        Long userId = getUserId();
        StpUtil.logout();
        log.debug("用户登出成功: userId={}", userId);
    }

    /**
     * 用户登出(指定账号ID)
     *
     * @param userId 用户ID
     */
    public static void logout(Long userId) {
        StpUtil.logout(userId);
        log.debug("用户被登出: userId={}", userId);
    }

    /**
     * 踢人下线(指定账号ID)
     * <p>
     * 将指定用户踢下线, 该用户的所有 Token 都会失效。
     * 与 logout 的区别: kick 会触发被踢下线事件, 且该用户会被记录到黑名单中一段时间。
     *
     * @param userId 用户ID
     */
    public static void kickout(Long userId) {
        StpUtil.kickout(userId);
        log.warn("用户被踢下线: userId={}", userId);
    }

    /**
     * 踢人下线(指定账号ID和设备类型)
     * <p>
     * 将指定用户在指定设备上的 Token 踢下线, 该用户的所有 Token 都会失效。
     * 与 logout 的区别: kick 会触发被踢下线事件, 且该用户会被记录到黑名单中一段时间。
     *
     * @param userId 用户ID
     * @param device 设备类型
     */
    public static void kickout(Long userId, String device) {
        StpUtil.kickout(userId, device);
        log.warn("用户被踢下线: userId={}, device={}", userId, device);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户ID(如果未登录则返回null)
     *
     * @return 用户ID或null
     */
    public static Long getUserIdOrNull() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser() {
        SaSession session = StpUtil.getSession();
        if (session == null) {
            return null;
        }
        return session.getModel(SaTokenConstants.LOGIN_USER_KEY, LoginUser.class);
    }

    /**
     * 获取当前登录用户信息(如果未登录则返回null)
     *
     * @return 登录用户信息或null
     */
    public static LoginUser getLoginUserOrNull() {
        try {
            return getLoginUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置当前登录用户信息
     * <p>
     * 更新 Session 中存储的用户信息, 用于信息变更后的及时更新。
     *
     * @param loginUser 登录用户信息
     */
    public static void setLoginUser(LoginUser loginUser) {
        SaSession session = StpUtil.getSession();
        session.set(SaTokenConstants.LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前登录用户昵称
     *
     * @return 昵称
     */
    public static String getNickname() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getNickname() : null;
    }

    /**
     * 获取当前登录用户的角色列表
     *
     * @return 角色编码集合
     */
    public static Set<String> getRoles() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getRoles() != null
                ? loginUser.getRoles()
                : Collections.emptySet();
    }

    /**
     * 获取当前登录用户的权限列表
     *
     * @return 权限编码集合
     */
    public static Set<String> getPermissions() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.getPermissions() != null
                ? loginUser.getPermissions()
                : Collections.emptySet();
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param role 角色编码
     * @return true-拥有, false-不拥有
     */
    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param permission 权限编码
     * @return true-拥有, false-不拥有
     */
    public static boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    /**
     * 判断当前用户是否拥有指定角色中的任意一个
     *
     * @param roles 角色编码数组
     * @return true-拥有任意一个, false-都不拥有
     */
    public static boolean hasAnyRole(String... roles) {
        Set<String> userRoles = getRoles();
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前用户是否拥有指定权限中的任意一个
     *
     * @param permissions 权限编码数组
     * @return true-拥有任意一个, false-都不拥有
     */
    public static boolean hasAnyPermission(String... permissions) {
        Set<String> userPermissions = getPermissions();
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取扩展数据
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    public static <T> T getExt(String key) {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getExt(key) : null;
    }

    /**
     * 获取扩展数据(带默认值)
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 值或默认值
     */
    public static <T> T getExt(String key, T defaultValue) {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getExt(key, defaultValue) : defaultValue;
    }

    /**
     * 设置扩展数据
     * <p>
     * 将数据存储到当前登录用户的扩展字段中, 并更新 Session。
     *
     * @param key   键
     * @param value 值
     */
    public static void putExt(String key, Object value) {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            loginUser.putExt(key, value);
            setLoginUser(loginUser);
        }
    }

    /**
     * 更新用户的角色列表
     * <p>
     * 当用户的角色发生变化时, 调用此方法更新 Session 中的角色信息。
     *
     * @param roles 新的角色列表
     */
    public static void updateRoles(Set<String> roles) {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            loginUser.setRoles(roles);
            setLoginUser(loginUser);
            log.debug("更新用户角色: userId={}, roles={}", loginUser.getUserId(), roles);
        }
    }

    /**
     * 更新用户的权限列表
     * <p>
     * 当用户的权限发生变化时, 调用此方法更新 Session 中的权限信息。
     *
     * @param permissions 新的权限列表
     */
    public static void updatePermissions(Set<String> permissions) {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            loginUser.setPermissions(permissions);
            setLoginUser(loginUser);
            log.debug("更新用户权限: userId={}, permissions size={}", loginUser.getUserId(), permissions.size());
        }
    }

    /**
     * 获取当前 Token 值
     *
     * @return Token 值
     */
    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    /**
     * 获取当前 Token 信息
     *
     * @return Token 信息对象
     */
    public static Object getTokenInfo() {
        return StpUtil.getTokenInfo();
    }

    /**
     * 判断当前是否已登录
     *
     * @return true-已登录, false-未登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 校验当前是否已登录, 如未登录则抛出异常
     */
    public static void checkLogin() {
        StpUtil.checkLogin();
    }

    /**
     * 判断指定用户是否为超级管理员
     *
     * @param userId 用户ID
     * @return true-是超级管理员, false-不是超级管理员
     */
    public static boolean isSuperAdmin(Long userId) {
        return SUPER_ADMIN_ID.equals(userId);
    }

    /**
     * 获取指定用户的在线 Token 列表
     *
     * @param userId 用户ID
     * @return Token 列表
     */
    public static List<String> getTokenValueListByUserId(Long userId) {
        return StpUtil.getTokenValueListByLoginId(userId);
    }

    /**
     * 获取当前用户的所有在线 Token 数量
     *
     * @return Token 数量
     */
    public static int getTokenCount() {
        return StpUtil.getTokenValueListByLoginId(getUserId()).size();
    }

    /**
     * 禁用指定用户
     * <p>
     * 将用户加入黑名单, 指定时间内无法登录。
     *
     * @param userId 用户ID
     * @param time   禁用时长(秒)
     */
    public static void disable(Long userId, long time) {
        StpUtil.disable(userId, time);
        log.warn("用户被禁用: userId={}, time={}秒", userId, time);
    }

    /**
     * 解除指定用户的禁用状态
     *
     * @param userId 用户ID
     */
    public static void undisable(Long userId) {
        StpUtil.untieDisable(userId);
        log.info("用户解除禁用: userId={}", userId);
    }

    /**
     * 判断指定用户是否被禁用
     *
     * @param userId 用户ID
     * @return true-已禁用, false-未禁用
     */
    public static boolean isDisable(Long userId) {
        return StpUtil.isDisable(userId);
    }

    /**
     * 获取指定用户的禁用剩余时间(秒)
     *
     * @param userId 用户ID
     * @return 剩余时间(秒), -1表示永久禁用, -2表示未被禁用
     */
    public static long getDisableTime(Long userId) {
        return StpUtil.getDisableTime(userId);
    }
}
