package cn.refinex.platform.controller.captcha;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.platform.controller.captcha.dto.response.CaptchaCreateResponseDTO;
import cn.refinex.platform.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 *
 * @author Refinex
 * @since 2025-10-05
 */
@SaIgnore
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "验证码生成等接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping
    @Operation(summary = "生成验证码", description = "生成验证码图片（Base64编码）和唯一标识")
    public ApiResponse<CaptchaCreateResponseDTO> generate() {
        CaptchaCreateResponseDTO response = captchaService.generate();
        return ApiResponse.success(response);
    }
}

