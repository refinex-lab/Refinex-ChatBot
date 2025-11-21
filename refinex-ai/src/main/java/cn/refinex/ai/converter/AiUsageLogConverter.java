package cn.refinex.ai.converter;

import cn.refinex.ai.controller.chat.dto.response.AiUsageLogResponseDTO;
import cn.refinex.ai.entity.AiUsageLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 使用日志转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiUsageLogConverter {

    AiUsageLogResponseDTO toResponse(AiUsageLog log);
}
