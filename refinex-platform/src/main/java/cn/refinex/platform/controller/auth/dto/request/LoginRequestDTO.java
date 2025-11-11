package cn.refinex.platform.controller.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
public class LoginRequestDTO {

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码UUID")
    private String captchaUuid;

    @Schema(description = "验证码内容")
    private String captchaCode;

    @Schema(description = "设备类型: PC/APP/H5")
    private String deviceType;

    @Schema(description = "记住我")
    private Boolean rememberMe = Boolean.FALSE;
}
