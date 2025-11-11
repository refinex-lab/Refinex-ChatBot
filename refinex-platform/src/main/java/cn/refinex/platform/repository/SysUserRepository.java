package cn.refinex.platform.repository;

import cn.refinex.core.util.BeanUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户仓储
 *
 * @author Refinex
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SysUserRepository {

    private final JdbcTemplateManager jdbcManager;

    /**
     * 根据用户 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户
     */
    public Optional<SysUser> findById(Long userId) {
        String sql = """
                SELECT *
                FROM sys_user
                WHERE id = :userId
                  AND deleted = 0
                """;
        Map<String, Object> params = Map.of("userId", userId);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, SysUser.class));
    }

    /**
     * 根据邮箱索引查询用户
     *
     * @param emailIndex 邮箱索引
     * @return 用户
     */
    public Optional<SysUser> findByEmailIndex(String emailIndex) {
        String sql = """
                SELECT *
                FROM sys_user
                WHERE email_index = :emailIndex
                  AND deleted = 0
                """;
        Map<String, Object> params = Map.of("emailIndex", emailIndex);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, SysUser.class));
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    public Optional<SysUser> findByUsername(String username) {
        String sql = """
                SELECT *
                FROM sys_user
                WHERE username = :username
                  AND deleted = 0
                """;
        Map<String, Object> params = Map.of("username", username);
        return Optional.ofNullable(jdbcManager.queryObject(sql, params, SysUser.class));
    }

    /**
     * 根据用户名检查用户是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = """
                SELECT COUNT(1)
                FROM sys_user
                WHERE username = :username
                  AND deleted = 0
                """;
        Integer count = jdbcManager.queryInt(sql, Map.of("username", username));
        return count != null && count > 0;
    }

    /**
     * 根据邮箱索引检查用户是否存在
     *
     * @param emailIndex 邮箱索引
     * @return 是否存在
     */
    public boolean existsByEmailIndex(String emailIndex) {
        String sql = """
                SELECT COUNT(1)
                FROM sys_user
                WHERE email_index = :emailIndex
                  AND deleted = 0
                """;
        Integer count = jdbcManager.queryInt(sql, Map.of("emailIndex", emailIndex));
        return count != null && count > 0;
    }

    /**
     * 插入用户
     *
     * @param user    用户
     * @param manager 数据库模板管理器
     */
    public void insert(SysUser user, JdbcTemplateManager manager) {
        JdbcTemplateManager executor = requireManager(manager);
        String sql = """
                INSERT INTO sys_user (
                    id, username, mobile_cipher, mobile_index, email_cipher, email_index, password,
                    nickname, sex, avatar, account_status, last_login_time, last_login_ip,
                    create_by, create_time, update_by, update_time, deleted, delete_by, delete_time,
                    remark, status)
                VALUES (
                    :id, :username, :mobileCipher, :mobileIndex, :emailCipher, :emailIndex, :password,
                    :nickname, :sex, :avatar, :accountStatus, :lastLoginTime, :lastLoginIp,
                    :createBy, :createTime, :updateBy, :updateTime, :deleted, :deleteBy, :deleteTime,
                    :remark, :status)
                """;

        Map<String, Object> params = BeanUtils.beanToMap(user, false, false);
        executor.update(sql, params);
    }

    /**
     * 更新用户最后登录信息
     *
     * @param userId    用户 ID
     * @param loginTime 登录时间
     * @param loginIp   登录 IP 地址
     * @param manager   数据库模板管理器
     */
    public void updateLastLoginInfo(Long userId, LocalDateTime loginTime, String loginIp, JdbcTemplateManager manager) {
        JdbcTemplateManager executor = requireManager(manager);
        String sql = """
                UPDATE sys_user
                SET last_login_time = :loginTime,
                    last_login_ip   = :loginIp,
                    update_time     = :updateTime
                WHERE id = :userId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("loginTime", loginTime);
        params.put("loginIp", loginIp);
        params.put("updateTime", LocalDateTime.now());
        params.put("userId", userId);
        executor.update(sql, params);
    }

    /**
     * 检查数据库模板管理器是否为空
     *
     * @param manager 数据库模板管理器
     * @return 数据库模板管理器
     */
    private JdbcTemplateManager requireManager(JdbcTemplateManager manager) {
        return Objects.requireNonNull(manager, "JdbcTemplateManager is required for write operations");
    }
}
