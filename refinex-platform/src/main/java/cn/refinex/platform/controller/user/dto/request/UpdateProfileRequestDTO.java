package cn.refinex.platform.controller.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人信息请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Schema(description = "更新个人信息请求")
public class UpdateProfileRequestDTO {

    @Schema(description = "昵称(1-50)")
    @Size(min = 1, max = 50, message = "昵称长度需在1-50之间")
    private String nickname;

    @Schema(description = "性别: MALE/FEMALE/OTHER")
    private String sex;

    @Schema(description = "头像URL(最长500)")
    @Size(max = 500, message = "头像URL长度不能超过500")
    private String avatar;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号(可选,原样存储)")
    @Size(max = 50, message = "手机号长度不能超过50")
    private String mobile;
}

