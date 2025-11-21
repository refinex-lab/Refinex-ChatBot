package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.chat.dto.request.AiUsageLogPageRequest;
import cn.refinex.ai.controller.chat.dto.response.AiUsageLogResponseDTO;
import cn.refinex.ai.converter.AiUsageLogConverter;
import cn.refinex.ai.entity.AiUsageLog;
import cn.refinex.ai.repository.AiUsageLogRepository;
import cn.refinex.ai.service.AiUsageLogService;
import cn.refinex.core.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * 使用日志服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiUsageLogServiceImpl implements AiUsageLogService {

    private final AiUsageLogRepository repository;
    private final AiUsageLogConverter converter;

    /**
     * 分页查询使用日志
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    @Override
    public PageResponse<AiUsageLogResponseDTO> page(AiUsageLogPageRequest request, Long userId) {
        AiUsageLogPageRequest query = Objects.isNull(request) ? new AiUsageLogPageRequest() : request;
        PageResponse<AiUsageLog> page = repository.page(userId, trimToNull(query.getOperation()), query.getSuccess(), query);
        return page.map(converter::toResponse);
    }
}
