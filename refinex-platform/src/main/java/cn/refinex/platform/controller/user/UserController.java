package cn.refinex.platform.controller.user;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.logging.annotation.RequestLog;
import cn.refinex.core.logging.enums.RequestLogType;
import cn.refinex.platform.controller.auth.dto.response.UserProfileDTO;
import cn.refinex.platform.controller.user.dto.request.ChangePasswordRequestDTO;
import cn.refinex.platform.controller.user.dto.request.UpdateProfileRequestDTO;
import cn.refinex.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人中心接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "用户中心管理", description = "个人信息/密码管理")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public ApiResponse<UserProfileDTO> profile() {
        return ApiResponse.success(userService.getProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    @Parameter(name = "body", description = "更新请求")
    @RequestLog(title = "更新个人信息", type = RequestLogType.UPDATE)
    public ApiResponse<UserProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileRequestDTO request) {
        return ApiResponse.success(userService.updateProfile(request));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @Parameter(name = "body", description = "修改密码请求")
    @RequestLog(title = "修改密码", type = RequestLogType.UPDATE)
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(request);
        return ApiResponse.success();
    }
}
