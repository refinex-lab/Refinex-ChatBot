package cn.refinex.ai.service;

import cn.refinex.ai.controller.model.dto.request.AiModelCreateRequestDTO;
import cn.refinex.ai.controller.model.dto.request.AiModelPageRequest;
import cn.refinex.ai.controller.model.dto.request.AiModelUpdateRequestDTO;
import cn.refinex.ai.controller.model.dto.response.AiModelResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * 模型服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiModelService {

    /**
     * 分页查询模型
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResponse<AiModelResponseDTO> page(AiModelPageRequest request);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 模型
     */
    Optional<AiModelResponseDTO> findById(Long id);

    /**
     * 创建模型
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    void create(AiModelCreateRequestDTO request, Long operatorId);

    /**
     * 更新模型
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    void update(Long id, AiModelUpdateRequestDTO request, Long operatorId);

    /**
     * 更新模型状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除模型
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    void delete(Long id, Long operatorId);
}
