package cn.refinex.ai.service;

import cn.refinex.ai.controller.schema.dto.request.AiSchemaCreateRequestDTO;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaPageRequest;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaUpdateRequestDTO;
import cn.refinex.ai.controller.schema.dto.response.AiSchemaResponseDTO;
import cn.refinex.core.api.PageResponse;

import java.util.Optional;

/**
 * Schema 服务
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface AiSchemaService {

    /**
     * 分页查询 Schema
     *
     * @param request 查询请求
     * @return Schema 列表
     */
    PageResponse<AiSchemaResponseDTO> page(AiSchemaPageRequest request);

    /**
     * 根据 ID 查询 Schema
     *
     * @param id 主键
     * @return Schema
     */
    Optional<AiSchemaResponseDTO> findById(Long id);

    /**
     * 新增 Schema
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    void create(AiSchemaCreateRequestDTO request, Long operatorId);

    /**
     * 更新 Schema
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    void update(Long id, AiSchemaUpdateRequestDTO request, Long operatorId);

    /**
     * 更新 Schema 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);

    /**
     * 删除 Schema
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    void delete(Long id, Long operatorId);
}
