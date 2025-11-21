package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.schema.dto.request.AiSchemaCreateRequestDTO;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaPageRequest;
import cn.refinex.ai.controller.schema.dto.request.AiSchemaUpdateRequestDTO;
import cn.refinex.ai.controller.schema.dto.response.AiSchemaResponseDTO;
import cn.refinex.ai.converter.AiSchemaConverter;
import cn.refinex.ai.entity.AiSchema;
import cn.refinex.ai.repository.AiSchemaRepository;
import cn.refinex.ai.service.AiSchemaService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.json.util.JsonUtils;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * Schema 服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiSchemaServiceImpl implements AiSchemaService {

    private final AiSchemaRepository repository;
    private final AiSchemaConverter converter;
    private final JsonUtils jsonUtils;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 分页查询 Schema
     *
     * @param request 查询请求
     * @return Schema 列表
     */
    @Override
    public PageResponse<AiSchemaResponseDTO> page(AiSchemaPageRequest request) {
        AiSchemaPageRequest query = Objects.isNull(request) ? new AiSchemaPageRequest() : request;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiSchema> page = repository.page(
                trimToNull(query.getSchemaType()), query.getStatus(),
                trimToNull(query.getKeyword()), query, userId
        );
        return page.map(converter::toResponse);
    }

    /**
     * 根据 ID 查询 Schema
     *
     * @param id 主键
     * @return Schema
     */
    @Override
    public Optional<AiSchemaResponseDTO> findById(Long id) {
        return repository.findById(id, LoginHelper.getUserId()).map(converter::toResponse);
    }

    /**
     * 新增 Schema
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void create(AiSchemaCreateRequestDTO request, Long operatorId) {
        String code = request.schemaCode().trim();
        if (repository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("Schema 编码已存在: " + code);
        }

        AiSchema entity = buildEntity(request, operatorId);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateBy(operatorId);
        entity.setCreateTime(now);
        entity.setUpdateBy(operatorId);
        entity.setUpdateTime(now);
        entity.setDeleted(0);

        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(entity, jdbc);
            entity.setId(id);
            return null;
        });
    }

    /**
     * 更新 Schema
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void update(Long id, AiSchemaUpdateRequestDTO request, Long operatorId) {
        AiSchema exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Schema 不存在或已删除"));

        String newCode = request.schemaCode().trim();
        if (!exist.getSchemaCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("Schema 编码已存在: " + newCode);
        }

        AiSchema entity = buildEntity(new AiSchemaCreateRequestDTO(
                request.schemaCode(), request.schemaName(), request.schemaType(),
                request.schemaJson(), request.version(), request.strictMode(),
                request.status(), request.remark()), operatorId
        );

        entity.setId(exist.getId());
        entity.setCreateBy(exist.getCreateBy());
        entity.setCreateTime(exist.getCreateTime());
        entity.setDeleted(exist.getDeleted());
        entity.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(entity, jdbc);
            return null;
        });
    }

    /**
     * 更新 Schema 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiSchema exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Schema 不存在或已删除"));

        int normalized = normalizeFlag(status, exist.getStatus());
        if (Objects.equals(exist.getStatus(), normalized)) {
            return;
        }

        exist.setStatus(normalized);
        exist.setUpdateBy(operatorId);
        exist.setUpdateTime(LocalDateTime.now());

        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(exist, jdbc);
            return null;
        });
    }

    /**
     * 删除 Schema
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiSchema exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Schema 不存在或已删除"));

        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建 Schema 实体
     *
     * @param request    创建请求
     * @param operatorId 操作人 ID
     * @return Schema 实体
     */
    private AiSchema buildEntity(AiSchemaCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiSchema.builder()
                .schemaCode(request.schemaCode().trim())
                .schemaName(request.schemaName().trim())
                .schemaType(request.schemaType().trim())
                .schemaJson(toJson(request.schemaJson()))
                .version(request.version() == null ? 1 : request.version())
                .strictMode(request.strictMode() == null ? 1 : request.strictMode())
                .status(normalizeFlag(request.status(), 1))
                .remark(trimToNull(request.remark()))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 将 Map 转换为 JSON 字符串
     *
     * @param map 输入 Map
     * @return JSON 字符串
     */
    private String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return jsonUtils.toJson(map);
    }

    /**
     * 标准化标志位
     *
     * @param value       输入值
     * @param defaultValue 默认值
     * @return 标准化后的值
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
