package cn.refinex.platform.controller.file;

import cn.refinex.core.api.ApiResponse;
import cn.refinex.core.api.ApiStatus;
import cn.refinex.platform.controller.file.dto.request.StorageConfigCreateRequestDTO;
import cn.refinex.platform.controller.file.dto.request.StorageConfigUpdateRequestDTO;
import cn.refinex.platform.controller.file.dto.response.StorageConfigResponseDTO;
import cn.refinex.platform.service.StorageConfigService;
import cn.refinex.satoken.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储配置管理接口
 *
 * @author Refinex
 * @since 1.0.0
 */
@RestController
@RequestMapping("/files/storage/configs")
@Tag(name = "存储配置管理")
@RequiredArgsConstructor
public class StorageConfigController {

    private final StorageConfigService service;

    @Operation(summary = "存储配置列表")
    @GetMapping
    public ApiResponse<List<StorageConfigResponseDTO>> list() {
        return ApiResponse.success(service.list());
    }

    @Operation(summary = "按编码查询存储配置")
    @GetMapping("/{code}")
    @Parameter(name = "code", description = "存储配置编码")
    public ApiResponse<StorageConfigResponseDTO> get(@PathVariable("code") String code) {
        return service.getByCode(code).map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(ApiStatus.NOT_FOUND, "存储配置不存在"));
    }

    @Operation(summary = "新增存储配置")
    @PostMapping
    @Parameter(name = "request", description = "存储配置创建请求")
    public ApiResponse<Void> create(@RequestBody StorageConfigCreateRequestDTO request) {
        service.create(request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "更新存储配置")
    @PutMapping("/{code}")
    @Parameters(value = {
            @Parameter(name = "code", description = "存储配置编码"),
            @Parameter(name = "request", description = "存储配置更新请求")
    })
    public ApiResponse<Void> update(@PathVariable("code") String code, @RequestBody StorageConfigUpdateRequestDTO request) {
        service.update(code, request, LoginHelper.getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除存储配置")
    @DeleteMapping("/{code}")
    @Parameter(name = "code", description = "存储配置编码")
    public ApiResponse<Void> delete(@PathVariable("code") String code) {
        service.delete(code, LoginHelper.getUserId());
        return ApiResponse.success();
    }
}
