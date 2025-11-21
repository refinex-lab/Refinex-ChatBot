package cn.refinex.ai.converter;

import cn.refinex.ai.controller.chat.dto.response.ChatSessionResponseDTO;
import cn.refinex.ai.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 会话转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatSessionConverter {

    ChatSessionResponseDTO toResponse(ChatSession entity);
}
