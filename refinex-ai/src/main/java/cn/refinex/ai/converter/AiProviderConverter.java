package cn.refinex.ai.converter;

import cn.refinex.ai.controller.provider.dto.response.AiProviderResponseDTO;
import cn.refinex.ai.entity.AiProvider;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 模型供应商对象转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiProviderConverter {

    /**
     * 实体转换为响应对象
     *
     * @param entity 实体
     * @return 响应对象
     */
    AiProviderResponseDTO toResponse(AiProvider entity);
}
