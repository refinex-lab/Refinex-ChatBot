package cn.refinex.platform.controller.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户信息
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "当前登录用户信息")
public class UserProfileDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "账户状态")
    private Integer accountStatus;

    @Schema(description = "启用状态")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "角色编码列表")
    private List<String> roles;

    @Schema(description = "权限编码列表")
    private List<String> permissions;
}
