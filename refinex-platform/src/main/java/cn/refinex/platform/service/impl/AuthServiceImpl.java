package cn.refinex.platform.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.constants.AuthRedisConstants;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.service.CryptoService;
import cn.refinex.core.service.CryptoService.EncryptedValue;
import cn.refinex.core.util.DeviceUtils;
import cn.refinex.core.util.ServletUtils;
import cn.refinex.core.util.SnowflakeIdUtils;
import cn.refinex.core.util.StringUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.config.properties.AuthLoginProperties;
import cn.refinex.platform.constants.AuthConstants;
import cn.refinex.platform.controller.auth.dto.request.LoginRequestDTO;
import cn.refinex.platform.controller.auth.dto.request.RegisterRequestDTO;
import cn.refinex.platform.controller.auth.dto.response.LoginResponseDTO;
import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;
import cn.refinex.platform.entity.SysUser;
import cn.refinex.platform.repository.SysRoleRepository;
import cn.refinex.platform.repository.SysUserRepository;
import cn.refinex.platform.repository.SysUserRoleRepository;
import cn.refinex.platform.service.AuthService;
import cn.refinex.platform.service.CaptchaService;
import cn.refinex.platform.service.LoginAuditService;
import cn.refinex.platform.service.UserAuthCacheService;
import cn.refinex.redis.core.RedisService;
import cn.refinex.satokrn.common.helper.LoginHelper;
import cn.refinex.satokrn.common.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 默认角色编码
     */
    private static final String DEFAULT_ROLE_CODE = "ROLE_USER";

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final UserAuthCacheService userAuthCacheService;
    private final AuthLoginProperties loginProperties;
    private final CaptchaService captchaService;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;
    private final SnowflakeIdUtils snowflakeIdUtils;
    private final RedisService redisService;
    private final LoginAuditService loginAuditService;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录响应参数
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        String identity = normalizeEmail(request.getEmail());
        ensureNotLocked(identity);
        verifyCaptchaIfNeeded(request.getCaptchaUuid(), request.getCaptchaCode());

        String emailIndex = calculateEmailIndex(identity);
        SysUser user = userRepository.findByEmailIndex(emailIndex).orElse(null);

        HttpServletRequest httpRequest = ServletUtils.getRequest();
        String loginIp = ServletUtils.getClientIp(httpRequest);
        String deviceType = DeviceUtils.getDeviceType(request.getDeviceType());
        String userAgent = resolveUserAgent(httpRequest);

        if (Objects.isNull(user)) {
            handleLoginFailure(null, identity, loginIp, deviceType, userAgent, "账号不存在或密码错误");
            log.error("登录失败，邮箱或密码错误，身份：{}，IP：{}，设备类型：{}，User-Agent：{}", identity, loginIp, deviceType, userAgent);
            throw new BusinessException("邮箱或密码错误");
        }

        checkAccountStatus(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFailure(user, identity, loginIp, deviceType, userAgent, "密码错误");
            log.error("登录失败，密码错误，身份：{}，IP：{}，设备类型：{}，User-Agent：{}", identity, loginIp, deviceType, userAgent);
            throw new BusinessException("密码错误");
        }

        resetFailCounter(identity);

        List<String> roles = userAuthCacheService.getUserRoles(user.getId());
        List<String> permissions = userAuthCacheService.getUserPermissions(user.getId());

        LoginUser loginUser = buildLoginUser(user, roles, permissions, loginIp, deviceType);

        SaLoginParameter parameter = SaLoginParameter.create()
                .setDeviceType(deviceType)
                .setIsLastingCookie(Boolean.TRUE.equals(request.getRememberMe()))
                .setExtra(AuthConstants.EXTRA_LOGIN_IDENTITY, identity)
                .setExtra(AuthConstants.EXTRA_LOGIN_IP, loginIp)
                .setExtra(AuthConstants.EXTRA_LOGIN_DEVICE, deviceType)
                .setExtra(AuthConstants.EXTRA_LOGIN_USER_AGENT, userAgent)
                .setExtra(AuthConstants.EXTRA_LOGIN_USERNAME, user.getUsername());

        LoginHelper.login(loginUser, parameter);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return LoginResponseDTO.builder()
                .tokenName(tokenInfo.getTokenName())
                .tokenValue(tokenInfo.getTokenValue())
                .expireIn(tokenInfo.getTokenTimeout())
                .user(buildUserProfile(user, roles, permissions))
                .build();
    }

    /**
     * 用户登出
     */
    @Override
    public void logout() {
        LoginHelper.logout();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     */
    @Override
    public void register(RegisterRequestDTO request) {
        validateRegisterRequest(request);

        if (!loginProperties.isAllowRegister()) {
            throw new BusinessException("当前环境暂未开放自助注册，请联系管理员");
        }

        verifyCaptchaIfNeeded(request.getCaptchaUuid(), request.getCaptchaCode());

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        String emailIndex = calculateEmailIndex(normalizedEmail);
        if (userRepository.existsByEmailIndex(emailIndex)) {
            throw new BusinessException("邮箱已注册");
        }

        EncryptedValue encryptedEmail = encryptEmail(normalizedEmail);
        Long defaultRoleId = roleRepository.findRoleIdByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new BusinessException("默认角色未配置，请联系管理员"));

        SysUser user = buildNewUser(username, request.getNickname(), encryptedEmail, request.getPassword());

        jdbcManager.executeInTransaction(jdbc -> {
            userRepository.insert(user, jdbc);
            userRoleRepository.insert(user.getId(), defaultRoleId, jdbc);
            return null;
        });

        log.debug("用户注册成功，userId={} username={}", user.getId(), user.getUsername());
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    @Override
    public UserProfileDTO currentUser() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (Objects.isNull(loginUser)) {
            throw new BusinessException(ApiStatus.UNAUTHORIZED, "未登录或登录已过期");
        }

        SysUser user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(ApiStatus.UNAUTHORIZED, "账号已不存在"));

        List<String> roles = userAuthCacheService.getUserRoles(user.getId());
        List<String> permissions = userAuthCacheService.getUserPermissions(user.getId());
        return buildUserProfile(user, roles, permissions);
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户 ID
     * @return 用户角色列表
     */
    @Override
    public List<String> getUserRoles(Long userId) {
        return userAuthCacheService.getUserRoles(userId);
    }

    /**
     * 获取用户权限列表
     *
     * @param userId 用户 ID
     * @return 用户权限列表
     */
    @Override
    public List<String> getUserPermissions(Long userId) {
        return userAuthCacheService.getUserPermissions(userId);
    }

    /**
     * 验证注册请求参数
     *
     * @param request 注册请求参数
     */
    private void validateRegisterRequest(RegisterRequestDTO request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (StringUtils.isBlank(request.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StringUtils.isBlank(request.getEmail())) {
            throw new BusinessException("邮箱不能为空");
        }
        if (StringUtils.isBlank(request.getPassword()) || StringUtils.isBlank(request.getConfirmPassword())) {
            throw new BusinessException("密码或确认密码不能为空");
        }
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (request.getPassword().length() < 8) {
            throw new BusinessException("密码长度至少 8 位");
        }
    }

    /**
     * 检查账号是否被锁定
     *
     * @param identity 登录身份（用户名/手机号/邮箱）
     */
    private void ensureNotLocked(String identity) {
        String key = AuthRedisConstants.buildLoginLockKey(identityKey(identity));
        if (Boolean.TRUE.equals(redisService.hasKey(key))) {
            long seconds = Optional.ofNullable(redisService.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            long minutes = Math.max(1, seconds / 60);
            throw new BusinessException(ApiStatus.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试（剩余约 " + minutes + " 分钟）");
        }
    }

    /**
     * 验证验证码（如果启用了验证码功能）
     *
     * @param uuid 验证码 UUID
     * @param code 验证码输入值
     */
    private void verifyCaptchaIfNeeded(String uuid, String code) {
        if (!loginProperties.isEnableCaptcha()) {
            return;
        }
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            throw new BusinessException("验证码不能为空");
        }
        captchaService.verify(uuid, code);
    }

    /**
     * 计算邮箱索引（用于加密存储）
     *
     * @param email 邮箱地址
     * @return 邮箱索引
     */
    private String calculateEmailIndex(String email) {
        try {
            return cryptoService.index(email);
        } catch (Exception e) {
            log.error("计算邮箱索引失败", e);
            throw new BusinessException("账号处理失败，请稍后再试");
        }
    }

    /**
     * 加密邮箱地址（用于存储）
     *
     * @param email 邮箱地址
     * @return 加密后的邮箱值
     */
    private EncryptedValue encryptEmail(String email) {
        try {
            return cryptoService.encrypt(email);
        } catch (Exception e) {
            log.error("邮箱加密失败", e);
            throw new BusinessException("账号处理失败，请稍后再试");
        }
    }

    /**
     * 检查账号状态（是否被删除、停用、异常）
     *
     * @param user 用户实体
     */
    private void checkAccountStatus(SysUser user) {
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException("账号已被删除");
        }
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }
        if (!Objects.equals(user.getAccountStatus(), 1)) {
            throw new BusinessException("账号状态异常，暂时无法登录");
        }
    }

    /**
     * 处理登录失败（记录失败次数、锁定账号等）
     *
     * @param user        用户实体
     * @param identity    登录身份（用户名/手机号/邮箱）
     * @param loginIp     登录 IP 地址
     * @param deviceType  设备类型（PC/MOBILE）
     * @param userAgent   用户代理字符串
     * @param detailMessage 登录失败详细消息
     */
    private void handleLoginFailure(SysUser user, String identity, String loginIp, String deviceType, String userAgent, String detailMessage) {
        // 记录登录失败次数
        String key = identityKey(identity);
        String failKey = AuthRedisConstants.buildLoginFailCountKey(key);
        Duration ttl = Optional.ofNullable(loginProperties.getFailRecordTtl()).orElse(Duration.ofMinutes(15));
        Long failCount = redisService.string().increment(failKey);
        redisService.expire(failKey, ttl);

        // 超过最大失败次数，锁定账号
        int maxAttempts = Math.max(0, loginProperties.getMaxFailAttempts());
        if (maxAttempts > 0 && failCount != null && failCount >= maxAttempts) {
            Duration lockDuration = Optional.ofNullable(loginProperties.getLockDuration()).orElse(Duration.ofMinutes(30));
            redisService.string().set(AuthRedisConstants.buildLoginLockKey(key), 1, lockDuration);
        }

        // 记录登录失败审计日志
        Long userId = user != null ? user.getId() : null;
        String username = user != null ? user.getUsername() : null;
        loginAuditService.recordLoginFailure(userId, username, identity, loginIp, deviceType, userAgent, detailMessage);
    }

    /**
     * 重置登录失败计数器（登录成功后调用）
     *
     * @param identity 登录身份（用户名/手机号/邮箱）
     */
    private void resetFailCounter(String identity) {
        String key = identityKey(identity);
        redisService.delete(AuthRedisConstants.buildLoginFailCountKey(key));
    }

    /**
     * 构建登录用户对象（包含角色、权限、登录信息等）
     *
     * @param user        用户实体
     * @param roles       用户角色列表
     * @param permissions 用户权限列表
     * @param loginIp     登录 IP 地址
     * @param deviceType  设备类型（PC/MOBILE）
     * @return 登录用户对象
     */
    private LoginUser buildLoginUser(SysUser user, List<String> roles, List<String> permissions, String loginIp, String deviceType) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setSex(user.getSex());
        loginUser.setAccountStatus(user.getAccountStatus());
        loginUser.setStatus(user.getStatus());
        loginUser.setLoginIp(loginIp);
        loginUser.setDeviceType(deviceType);
        loginUser.setEmail(maskEmail(decryptEmail(user.getEmailCipher())));
        loginUser.setRoles(CollectionUtils.isEmpty(roles) ? new HashSet<>() : new HashSet<>(roles));
        loginUser.setPermissions(CollectionUtils.isEmpty(permissions) ? new HashSet<>() : new HashSet<>(permissions));
        return loginUser;
    }

    /**
     * 构建用户配置文件对象（包含用户信息、角色、权限）
     *
     * @param user        用户实体
     * @param roles       用户角色列表
     * @param permissions 用户权限列表
     * @return 用户配置文件对象
     */
    private UserProfileDTO buildUserProfile(SysUser user, List<String> roles, List<String> permissions) {
        return UserProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(decryptEmail(user.getEmailCipher()))
                .mobile(null)
                .sex(user.getSex())
                .accountStatus(user.getAccountStatus())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .roles(new ArrayList<>(Optional.ofNullable(roles).orElseGet(List::of)))
                .permissions(new ArrayList<>(Optional.ofNullable(permissions).orElseGet(List::of)))
                .build();
    }

    /**
     * 解密用户邮箱（从数据库存储的密文解密）
     *
     * @param cipher 加密后的邮箱密文
     * @return 解密后的邮箱
     */
    private String decryptEmail(String cipher) {
        if (StringUtils.isBlank(cipher)) {
            return null;
        }
        try {
            return cryptoService.decrypt(cipher);
        } catch (Exception e) {
            log.warn("邮箱解密失败", e);
            return null;
        }
    }

    /**
     * 对用户邮箱进行脱敏处理（显示部分字符）
     *
     * @param email 原始邮箱地址
     * @return 脱敏后的邮箱地址
     */
    private String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) {
            return local.charAt(0) + "****@" + parts[1];
        }
        return local.charAt(0) + "****" + local.charAt(local.length() - 1) + "@" + parts[1];
    }

    /**
     * 构建新用户实体（包含加密后的邮箱和密码）
     *
     * @param username    用户名
     * @param nickname    昵称
     * @param encryptedEmail 加密后的邮箱
     * @param rawPassword 原始密码
     * @return 新用户实体
     */
    private SysUser buildNewUser(String username, String nickname, EncryptedValue encryptedEmail, String rawPassword) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = snowflakeIdUtils.nextId();
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(username);
        user.setNickname(StringUtils.blankToDefault(nickname, username));
        user.setEmailCipher(encryptedEmail.cipher());
        user.setEmailIndex(encryptedEmail.index());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setSex("OTHER");
        user.setAccountStatus(1);
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateBy(userId);
        user.setCreateTime(now);
        user.setUpdateBy(userId);
        user.setUpdateTime(now);
        return user;
    }

    /**
     * 从 HTTP 请求中解析用户代理字符串（User-Agent）
     *
     * @param request HTTP 请求对象
     * @return 用户代理字符串（User-Agent）
     */
    private String resolveUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader(HttpHeaders.USER_AGENT);
    }

    /**
     * 对用户邮箱进行标准化处理（转换为小写、trim 空格）
     *
     * @param email 原始邮箱地址
     * @return 标准化后的邮箱地址
     */
    private String normalizeEmail(String email) {
        return StringUtils.trim(email).toLowerCase(Locale.ROOT);
    }

    /**
     * 构建用户登录身份键（标准化邮箱或用户名）
     *
     * @param identity 原始登录身份（邮箱或用户名）
     * @return 标准化后的登录身份键
     */
    private String identityKey(String identity) {
        return normalizeEmail(identity);
    }
}
