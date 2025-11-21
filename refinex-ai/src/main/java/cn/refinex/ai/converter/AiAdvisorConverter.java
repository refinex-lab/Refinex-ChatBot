package cn.refinex.ai.converter;

import cn.refinex.ai.controller.advisor.dto.response.AiAdvisorResponseDTO;
import cn.refinex.ai.entity.AiAdvisor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Advisor 转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiAdvisorConverter {

    /**
     * 转换为 Advisor 响应
     *
     * @param entity Advisor 实体
     * @return Advisor 响应
     */
    AiAdvisorResponseDTO toResponse(AiAdvisor entity);
}
