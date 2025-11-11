package cn.refinex.platform.controller.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
public class RegisterRequestDTO {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度应在3-32位之间")
    private String username;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度应在8-64位之间")
    private String password;

    @Schema(description = "确认密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "验证码UUID")
    private String captchaUuid;

    @Schema(description = "验证码内容")
    private String captchaCode;
}
