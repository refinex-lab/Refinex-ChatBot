package cn.refinex.ai.converter;

import cn.refinex.ai.controller.model.dto.response.AiModelResponseDTO;
import cn.refinex.ai.entity.AiModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 模型对象转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiModelConverter {

    /**
     * 实体转换为响应对象
     *
     * @param entity 实体
     * @return 响应对象
     */
    AiModelResponseDTO toResponse(AiModel entity);
}
