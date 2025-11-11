package cn.refinex.platform.controller.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResponseDTO {

    @Schema(description = "Token 名称")
    private String tokenName;

    @Schema(description = "Token 值")
    private String tokenValue;

    @Schema(description = "Token 过期时间（秒）")
    private long expireIn;

    @Schema(description = "用户信息")
    private UserProfileDTO user;
}
