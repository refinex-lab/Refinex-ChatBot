package cn.refinex.platform.service.impl;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.service.CryptoService;
import cn.refinex.core.service.CryptoService.EncryptedValue;
import cn.refinex.core.util.StringUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;
import cn.refinex.platform.controller.user.dto.request.ChangePasswordRequestDTO;
import cn.refinex.platform.controller.user.dto.request.UpdateProfileRequestDTO;
import cn.refinex.platform.entity.SysUser;
import cn.refinex.platform.enums.UserSex;
import cn.refinex.platform.repository.SysUserRepository;
import cn.refinex.platform.repository.command.UpdateBasicInfoCommand;
import cn.refinex.platform.service.UserAuthCacheService;
import cn.refinex.platform.service.UserService;
import cn.refinex.satoken.common.helper.LoginHelper;
import cn.refinex.satoken.common.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户服务实现（个人中心）
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository userRepository;
    private final UserAuthCacheService userAuthCacheService;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 获取个人中心信息
     *
     * @return 个人中心信息
     */
    @Override
    public UserProfileDTO getProfile() {
        SysUser user = loadCurrentUserOrThrow();
        List<String> roles = userAuthCacheService.getUserRoles(user.getId());
        List<String> permissions = userAuthCacheService.getUserPermissions(user.getId());
        return buildUserProfile(user, roles, permissions);
    }

    /**
     * 更新个人中心信息
     *
     * @param request 更新信息
     * @return 个人中心信息
     */
    @Override
    public UserProfileDTO updateProfile(UpdateProfileRequestDTO request) {
        Long userId = LoginHelper.getUserId();
        SysUser user = loadCurrentUserOrThrow();

        String nickname = StringUtils.blankToDefault(request.getNickname(), user.getNickname());
        String sex = normalizeSex(request.getSex(), user.getSex());
        String avatar = request.getAvatar() != null ? request.getAvatar() : user.getAvatar();

        EncPair email = resolveEmail(user, request.getEmail(), userId);
        EncPair mobile = resolveMobile(user, request.getMobile(), userId);

        // 执行更新
        UpdateBasicInfoCommand cmd = UpdateBasicInfoCommand.builder()
                .userId(userId)
                .nickname(nickname)
                .sex(sex)
                .avatar(avatar)
                .emailCipher(email.cipher)
                .emailIndex(email.index)
                .mobileCipher(mobile.cipher)
                .mobileIndex(mobile.index)
                .updateBy(userId)
                .build();
        jdbcManager.executeInTransaction(jdbc -> {
            userRepository.updateBasicInfo(cmd, jdbc);
            return null;
        });

        updateLoginSessionProfile(nickname, avatar, sex);

        // 返回最新信息
        SysUser refreshed = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ApiStatus.NOT_FOUND, "账号不存在或已被删除"));
        List<String> roles = userAuthCacheService.getUserRoles(userId);
        List<String> permissions = userAuthCacheService.getUserPermissions(userId);
        return buildUserProfile(refreshed, roles, permissions);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     */
    @Override
    public void changePassword(ChangePasswordRequestDTO request) {
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        Long userId = LoginHelper.getUserId();
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ApiStatus.NOT_FOUND, "账号不存在或已被删除"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());
        jdbcManager.executeInTransaction(jdbc -> {
            userRepository.updatePassword(userId, encoded, userId, jdbc);
            return null;
        });

        // 修改密码后强制下线
        try {
            LoginHelper.logout();
        } catch (Exception e) {
            log.warn("修改密码后强制下线失败(忽略): {}", e.getMessage());
        }
    }

    /**
     * 邮箱标准化
     *
     * @param email 邮箱
     * @return 标准化后的邮箱
     */
    private String normalizeEmail(String email) {
        return StringUtils.trim(email).toLowerCase(Locale.ROOT);
    }

    /**
     * 加载当前登录用户
     *
     * @return 当前登录用户
     */
    private SysUser loadCurrentUserOrThrow() {
        return userRepository.findById(LoginHelper.getUserId())
                .orElseThrow(() -> new BusinessException(ApiStatus.NOT_FOUND, "账号不存在或已被删除"));
    }

    /**
     * 性别标准化
     *
     * @param requested 请求的性别
     * @param original  原始性别
     * @return 标准化的性别
     */
    private String normalizeSex(String requested, String original) {
        String value = StringUtils.blankToDefault(requested, original);
        if (StringUtils.isNotBlank(value) && !UserSex.ALL_CODES.contains(value)) {
            throw new BusinessException("性别不合法，必须为 MALE/FEMALE/OTHER");
        }
        return value;
    }

    /**
     * 解析邮箱更新（含唯一性校验）
     *
     * @param user 当前用户
     * @param requestedEmail 邮箱
     * @param userId 用户ID
     * @return 解析结果
     */
    private EncPair resolveEmail(SysUser user, String requestedEmail, Long userId) {
        String currentCipher = user.getEmailCipher();
        String currentIndex = user.getEmailIndex();
        if (StringUtils.isBlank(requestedEmail)) {
            return new EncPair(currentCipher, currentIndex);
        }

        String normalizedEmail = normalizeEmail(requestedEmail);

        try {
            String newIndex = cryptoService.index(normalizedEmail);
            if (Objects.equals(newIndex, currentIndex)) {
                return new EncPair(currentCipher, currentIndex);
            }
            Optional<SysUser> existed = userRepository.findByEmailIndex(newIndex);
            if (existed.isPresent() && !Objects.equals(existed.get().getId(), userId)) {
                throw new BusinessException("该邮箱已被其他账号占用");
            }
            EncryptedValue enc = cryptoService.encrypt(normalizedEmail);
            return new EncPair(enc.cipher(), enc.index());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理邮箱失败", e);
            throw new BusinessException("邮箱处理失败，请稍后再试");
        }
    }

    /**
     * 解析手机号更新（含唯一性校验）
     *
     * @param user 当前用户
     * @param requestedMobile 手机号
     * @param userId 用户ID
     * @return 解析结果
     */
    private EncPair resolveMobile(SysUser user, String requestedMobile, Long userId) {
        String currentCipher = user.getMobileCipher();
        String currentIndex = user.getMobileIndex();
        if (StringUtils.isBlank(requestedMobile)) {
            return new EncPair(currentCipher, currentIndex);
        }

        String newMobile = requestedMobile.trim();

        try {
            String newIndex = cryptoService.index(newMobile);
            if (Objects.equals(newIndex, currentIndex)) {
                return new EncPair(currentCipher, currentIndex);
            }
            Optional<SysUser> existed = userRepository.findByMobileIndex(newIndex);
            if (existed.isPresent() && !Objects.equals(existed.get().getId(), userId)) {
                throw new BusinessException("该手机号已被其他账号占用");
            }
            EncryptedValue enc = cryptoService.encrypt(newMobile);
            return new EncPair(enc.cipher(), enc.index());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理手机号失败", e);
            throw new BusinessException("手机号处理失败，请稍后再试");
        }
    }

    /**
     * 密文对
     */
    private record EncPair(String cipher, String index) {}

    /**
     * 解密（可能为空）
     *
     * @param cipher 密文
     * @return 明文
     */
    private String decryptNullable(String cipher) {
        if (StringUtils.isBlank(cipher)) {
            return null;
        }
        try {
            return cryptoService.decrypt(cipher);
        } catch (Exception e) {
            log.warn("解密失败，返回空", e);
            return null;
        }
    }

    /**
     * 构建用户信息
     *
     * @param user      用户
     * @param roles     角色
     * @param permissions 权限
     * @return 用户信息
     */
    private UserProfileDTO buildUserProfile(SysUser user, List<String> roles, List<String> permissions) {
        return UserProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(decryptNullable(user.getEmailCipher()))
                .mobile(decryptNullable(user.getMobileCipher()))
                .sex(user.getSex())
                .accountStatus(user.getAccountStatus())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .roles(new ArrayList<>(CollectionUtils.isEmpty(roles) ? List.of() : roles))
                .permissions(new ArrayList<>(CollectionUtils.isEmpty(permissions) ? List.of() : permissions))
                .build();
    }

    /**
     * 更新会话中的用户可见信息（昵称/头像/性别）
     *
     * @param nickname 昵称
     * @param avatar   头像
     * @param sex      性别
     */
    private void updateLoginSessionProfile(String nickname, String avatar, String sex) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null) {
            return;
        }
        if (StringUtils.isNotBlank(nickname)) {
            loginUser.setNickname(nickname);
        }
        loginUser.setAvatar(avatar);
        loginUser.setSex(sex);
        LoginHelper.setLoginUser(loginUser);
    }
}
