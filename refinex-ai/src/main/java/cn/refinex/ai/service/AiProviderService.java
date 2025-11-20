package cn.refinex.ai.service;

import cn.refinex.ai.controller.provider.dto.request.AiProviderCreateRequestDTO;
import cn.refinex.ai.controller.provider.dto.request.AiProviderPageRequest;
import cn.refinex.ai.controller.provider.dto.request.AiProviderUpdateRequestDTO;
import cn.refinex.ai.controller.provider.dto.response.AiProviderResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * 模型供应商服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiProviderService {

    /**
     * 分页查询模型供应商
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResponse<AiProviderResponseDTO> page(AiProviderPageRequest request);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 模型供应商
     */
    Optional<AiProviderResponseDTO> findById(Long id);

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 模型供应商
     */
    Optional<AiProviderResponseDTO> findByCode(String code);

    /**
     * 创建模型供应商
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    void create(AiProviderCreateRequestDTO request, Long operatorId);

    /**
     * 更新模型供应商
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    void update(Long id, AiProviderUpdateRequestDTO request, Long operatorId);

    /**
     * 更新供应商状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除模型供应商
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    void delete(Long id, Long operatorId);
}
