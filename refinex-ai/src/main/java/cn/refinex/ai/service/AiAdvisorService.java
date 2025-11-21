package cn.refinex.ai.service;

import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorCreateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorPageRequest;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorUpdateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.response.AiAdvisorResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * Advisor 服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiAdvisorService {

    /**
     * 分页查询 Advisor
     *
     * @param request 分页请求
     * @return Advisor 列表
     */
    PageResponse<AiAdvisorResponseDTO> page(AiAdvisorPageRequest request);

    /**
     * 根据主键查询 Advisor
     *
     * @param id 主键
     * @return Advisor
     */
    Optional<AiAdvisorResponseDTO> findById(Long id);

    /**
     * 新增 Advisor
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    void create(AiAdvisorCreateRequestDTO request, Long operatorId);

    /**
     * 更新 Advisor
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    void update(Long id, AiAdvisorUpdateRequestDTO request, Long operatorId);

    /**
     * 更新 Advisor 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除 Advisor
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    void delete(Long id, Long operatorId);
}
