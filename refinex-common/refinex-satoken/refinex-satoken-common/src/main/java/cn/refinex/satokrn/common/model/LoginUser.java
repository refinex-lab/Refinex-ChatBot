package cn.refinex.satokrn.common.model;

import cn.refinex.satokrn.common.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 登录用户信息
 * <p>
 * 封装登录用户的核心信息, 存储在 Sa-Token 的 Session 中。
 * 包含用户基本信息、角色权限信息、登录状态信息以及扩展字段。
 * <p>
 * 设计说明:
 * 1. 实现 Serializable 接口,支持 Redis 序列化存储
 * 2. 敏感字段(手机号、邮箱)仅存储脱敏后的数据
 * 3. 使用 Set 存储角色和权限,避免重复且便于权限校验
 * 4. 提供 extData 扩展字段,支持业务自定义数据存储
 * 5. 使用 Jackson 注解控制 JSON 序列化格式
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * <p>
     * 主键,唯一标识用户身份
     */
    private Long userId;

    /**
     * 用户名
     * <p>
     * 登录账号,用于登录认证
     */
    private String username;

    /**
     * 用户昵称
     * <p>
     * 显示名称,用于UI展示
     */
    private String nickname;

    /**
     * 用户头像
     * <p>
     * 头像图片URL地址
     */
    private String avatar;

    /**
     * 性别
     * <p>
     * 枚举值: MALE-男, FEMALE-女, OTHER-其他
     */
    private String sex;

    /**
     * 手机号(脱敏)
     * <p>
     * 存储脱敏后的手机号,如: 138****5678
     * 注意: 不存储原始手机号,避免泄露
     */
    private String mobile;

    /**
     * 邮箱(脱敏)
     * <p>
     * 存储脱敏后的邮箱,如: a****b@example.com
     * 注意: 不存储原始邮箱,避免泄露
     */
    private String email;

    /**
     * 账户状态
     * <p>
     * 1-正常, 2-冻结, 3-注销
     */
    private Integer accountStatus;

    /**
     * 启用状态
     * <p>
     * 1-启用, 0-停用
     */
    private Integer status;

    /**
     * 角色编码列表
     * <p>
     * 存储用户拥有的所有角色编码,如: ["ROLE_ADMIN", "ROLE_USER"]
     * 使用 Set 避免重复,且便于权限校验时的 contains 操作
     */
    private Set<String> roles = new HashSet<>();

    /**
     * 权限编码列表
     * <p>
     * 存储用户拥有的所有权限编码,如: ["system:user:add", "system:role:edit"]
     * 使用 Set 避免重复,且便于权限校验时的 contains 操作
     */
    private Set<String> permissions = new HashSet<>();

    /**
     * 登录时间
     * <p>
     * 记录用户本次登录的时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    /**
     * 登录IP地址
     * <p>
     * 记录用户本次登录的IP地址
     */
    private String loginIp;

    /**
     * 登录设备类型
     * <p>
     * 如: PC, MOBILE, TABLET
     */
    private String deviceType;

    /**
     * 扩展数据
     * <p>
     * 用于存储业务自定义的额外信息,避免频繁修改 LoginUser 类
     * <p>
     * 使用示例:
     * - 存储部门信息: extData.put("deptId", 100L)
     * - 存储岗位信息: extData.put("postId", 200L)
     * - 存储租户信息: extData.put("tenantId", "tenant_001")
     */
    private transient Map<String, Object> extData = new HashMap<>();

    // ==================== 便捷方法 ====================

    /**
     * 添加角色
     *
     * @param role 角色编码
     * @return this
     */
    public LoginUser addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
        return this;
    }

    /**
     * 添加权限
     *
     * @param permission 权限编码
     * @return this
     */
    public LoginUser addPermission(String permission) {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.add(permission);
        return this;
    }

    /**
     * 设置扩展数据
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public LoginUser putExt(String key, Object value) {
        if (this.extData == null) {
            this.extData = new HashMap<>();
        }
        this.extData.put(key, value);
        return this;
    }

    /**
     * 获取扩展数据
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExt(String key) {
        if (this.extData == null) {
            return null;
        }
        return (T) this.extData.get(key);
    }

    /**
     * 获取扩展数据,带默认值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExt(String key, T defaultValue) {
        if (this.extData == null || !this.extData.containsKey(key)) {
            return defaultValue;
        }
        return (T) this.extData.get(key);
    }

    /**
     * 判断是否拥有指定角色
     *
     * @param role 角色编码
     * @return true-拥有, false-不拥有
     */
    public boolean hasRole(String role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * 判断是否拥有指定权限
     *
     * @param permission 权限编码
     * @return true-拥有, false-不拥有
     */
    public boolean hasPermission(String permission) {
        return this.permissions != null && this.permissions.contains(permission);
    }

    /**
     * 判断账户是否正常
     *
     * @return true-正常, false-异常
     */
    public boolean isAccountNormal() {
        return accountStatus != null && accountStatus.equals(AccountStatus.NORMAL.getCode());
    }

    /**
     * 判断是否启用
     *
     * @return true-启用, false-停用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

}
