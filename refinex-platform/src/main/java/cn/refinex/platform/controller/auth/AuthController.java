package cn.refinex.platform.controller.auth;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.refinex.core.api.ApiResponse;
import cn.refinex.platform.controller.auth.dto.request.LoginRequestDTO;
import cn.refinex.platform.controller.auth.dto.request.RegisterRequestDTO;
import cn.refinex.platform.controller.auth.dto.response.LoginResponseDTO;
import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;
import cn.refinex.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "登录/注册/角色权限接口")
public class AuthController {

    private final AuthService authService;

    @SaIgnore
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }

    @SaIgnore
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息")
    public ApiResponse<UserProfileDTO> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }

    @SaIgnore
    @GetMapping("/roles/{userId}")
    @Operation(summary = "查询用户角色（供内部调用）")
    public ApiResponse<List<String>> roles(@PathVariable("userId") Long userId) {
        return ApiResponse.success(authService.getUserRoles(userId));
    }

    @SaIgnore
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "查询用户权限（供内部调用）")
    public ApiResponse<List<String>> permissions(@PathVariable("userId") Long userId) {
        return ApiResponse.success(authService.getUserPermissions(userId));
    }
}
