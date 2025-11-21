package cn.refinex.ai.service;

import cn.refinex.ai.controller.tool.dto.request.AiToolCreateRequestDTO;
import cn.refinex.ai.controller.tool.dto.request.AiToolPageRequest;
import cn.refinex.ai.controller.tool.dto.request.AiToolUpdateRequestDTO;
import cn.refinex.ai.controller.tool.dto.response.AiToolResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * 工具服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiToolService {

    /**
     * 分页查询工具
     *
     * @param request 查询请求
     * @return 工具列表
     */
    PageResponse<AiToolResponseDTO> page(AiToolPageRequest request);

    /**
     * 根据 ID 查询工具
     *
     * @param id 主键
     * @return 工具
     */
    Optional<AiToolResponseDTO> findById(Long id);

    /**
     * 新增工具
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    void create(AiToolCreateRequestDTO request, Long operatorId);

    /**
     * 更新工具
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    void update(Long id, AiToolUpdateRequestDTO request, Long operatorId);

    /**
     * 更新工具状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除工具
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    void delete(Long id, Long operatorId);
}
