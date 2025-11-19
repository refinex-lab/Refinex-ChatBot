package cn.refinex.ai.service;

import cn.refinex.ai.controller.prompt.dto.request.AiPromptCreateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptPageRequest;
import cn.refinex.ai.controller.prompt.dto.request.AiPromptUpdateRequestDTO;
import cn.refinex.ai.controller.prompt.dto.response.AiPromptResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * AI 提示词服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiPromptService {

    /**
     * 分页查询提示词
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResponse<AiPromptResponseDTO> page(AiPromptPageRequest request);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 提示词
     */
    Optional<AiPromptResponseDTO> findById(Long id);

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 提示词
     */
    Optional<AiPromptResponseDTO> findByCode(String code);

    /**
     * 创建提示词
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    void create(AiPromptCreateRequestDTO request, Long operatorId);

    /**
     * 更新提示词
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    void update(Long id, AiPromptUpdateRequestDTO request, Long operatorId);

    /**
     * 删除提示词
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    void delete(Long id, Long operatorId);
}
