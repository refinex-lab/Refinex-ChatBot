package cn.refinex.ai.controller.provider.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 模型供应商响应对象
 *
 * @author Refinex
 * @since 1.0.0
 */
@Schema(description = "模型供应商响应")
public record AiProviderResponseDTO(

        @Schema(description = "主键ID")
        Long id,

        @Schema(description = "供应商编码")
        String providerCode,

        @Schema(description = "供应商名称")
        String providerName,

        @Schema(description = "供应商类型")
        String providerType,

        @Schema(description = "基础 URL")
        String baseUrl,

        @Schema(description = "API Key 密文(AES-GCM)")
        String apiKeyCipher,

        @Schema(description = "API Key 索引(如KMS别名/HMAC)")
        String apiKeyIndex,

        @Schema(description = "限流: QPM")
        Integer rateLimitQpm,

        @Schema(description = "状态:1启用,0停用")
        Integer status,

        @Schema(description = "备注")
        String remark,

        @Schema(description = "创建人")
        Long createBy,

        @Schema(description = "创建时间")
        LocalDateTime createTime,

        @Schema(description = "更新人")
        Long updateBy,

        @Schema(description = "更新时间")
        LocalDateTime updateTime,

        @Schema(description = "删除人")
        Long deleteBy,

        @Schema(description = "删除时间")
        LocalDateTime deleteTime
) {
}
