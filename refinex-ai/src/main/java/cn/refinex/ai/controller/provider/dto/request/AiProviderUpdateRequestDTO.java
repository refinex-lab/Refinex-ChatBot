package cn.refinex.ai.controller.provider.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新模型供应商请求
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "更新模型供应商请求")
public record AiProviderUpdateRequestDTO(

        @NotBlank(message = "供应商编码不能为空")
        @Size(max = 50, message = "供应商编码长度不能超过50")
        @Schema(description = "供应商编码")
        String providerCode,

        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 100, message = "供应商名称长度不能超过100")
        @Schema(description = "供应商名称")
        String providerName,

        @Size(max = 30, message = "供应商类型长度不能超过30")
        @Schema(description = "供应商类型")
        String providerType,

        @Size(max = 255, message = "基础URL长度不能超过255")
        @Schema(description = "基础 URL")
        String baseUrl,

        @Size(max = 1024, message = "API Key 密文长度不能超过1024")
        @Schema(description = "API Key 密文(AES-GCM)")
        String apiKeyCipher,

        @Size(max = 128, message = "API Key 索引长度不能超过128")
        @Schema(description = "API Key 索引(如KMS别名/HMAC)")
        String apiKeyIndex,

        @Schema(description = "限流: QPM")
        Integer rateLimitQpm,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Size(max = 500, message = "备注长度不能超过500")
        @Schema(description = "备注")
        String remark
) {
}
