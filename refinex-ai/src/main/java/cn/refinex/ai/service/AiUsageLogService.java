package cn.refinex.ai.service;

import cn.refinex.ai.controller.chat.dto.request.AiUsageLogPageRequest;
import cn.refinex.ai.controller.chat.dto.response.AiUsageLogResponseDTO;
import cn.refinex.core.api.PageResponse;

/**
 * 使用日志服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiUsageLogService {

    /**
     * 分页查询使用日志
     *
     * @param request 分页查询请求
     * @param userId  用户ID
     * @return 分页查询响应
     */
    PageResponse<AiUsageLogResponseDTO> page(AiUsageLogPageRequest request, Long userId);
}
