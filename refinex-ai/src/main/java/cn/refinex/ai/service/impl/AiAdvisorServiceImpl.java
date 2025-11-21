package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorCreateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorPageRequest;
import cn.refinex.ai.controller.advisor.dto.request.AiAdvisorUpdateRequestDTO;
import cn.refinex.ai.controller.advisor.dto.response.AiAdvisorResponseDTO;
import cn.refinex.ai.converter.AiAdvisorConverter;
import cn.refinex.ai.entity.AiAdvisor;
import cn.refinex.ai.repository.AiAdvisorRepository;
import cn.refinex.ai.service.AiAdvisorService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * Advisor 服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiAdvisorServiceImpl implements AiAdvisorService {

    private final AiAdvisorRepository repository;
    private final AiAdvisorConverter converter;
    private final JdbcTemplateManager jdbcManager;

    /**
     * 分页查询 Advisor
     *
     * @param request 分页请求
     * @return Advisor 列表
     */
    @Override
    public PageResponse<AiAdvisorResponseDTO> page(AiAdvisorPageRequest request) {
        AiAdvisorPageRequest query = Objects.isNull(request) ? new AiAdvisorPageRequest() : request;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiAdvisor> page = repository.page(
                trimToNull(query.getAdvisorType()), query.getStatus(),
                trimToNull(query.getKeyword()), query, userId
        );
        return page.map(converter::toResponse);
    }

    /**
     * 根据 ID 查询 Advisor
     *
     * @param id 主键
     * @return Advisor
     */
    @Override
    public Optional<AiAdvisorResponseDTO> findById(Long id) {
        return repository.findById(id, LoginHelper.getUserId()).map(converter::toResponse);
    }

    /**
     * 新增 Advisor
     *
     * @param request    新增请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void create(AiAdvisorCreateRequestDTO request, Long operatorId) {
        String code = request.advisorCode().trim();
        if (repository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("Advisor 编码已存在: " + code);
        }

        AiAdvisor entity = buildEntity(request, operatorId);
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
     * 更新 Advisor
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人 ID
     */
    @Override
    public void update(Long id, AiAdvisorUpdateRequestDTO request, Long operatorId) {
        AiAdvisor exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Advisor 不存在或已删除"));

        String newCode = request.advisorCode().trim();
        if (!exist.getAdvisorCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("Advisor 编码已存在: " + newCode);
        }

        AiAdvisor entity = buildEntity(new AiAdvisorCreateRequestDTO(
                request.advisorCode(), request.advisorName(), request.advisorType(),
                request.sort(), request.status(), request.remark()), operatorId);

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
     * 更新 Advisor 状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人 ID
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiAdvisor exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Advisor 不存在或已删除"));

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
     * 删除 Advisor
     *
     * @param id         主键
     * @param operatorId 操作人 ID
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiAdvisor exist = repository
                .findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("Advisor 不存在或已删除"));

        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建 Advisor 实体
     *
     * @param request    创建请求
     * @param operatorId 操作人 ID
     * @return Advisor 实体
     */
    private AiAdvisor buildEntity(AiAdvisorCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiAdvisor.builder()
                .advisorCode(request.advisorCode().trim())
                .advisorName(request.advisorName().trim())
                .advisorType(request.advisorType().trim())
                .sort(request.sort() == null ? 0 : request.sort())
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
     * 归一化状态值
     *
     * @param value       状态值
     * @param defaultValue 默认值
     * @return 归一化后的状态值
     */
    private int normalizeFlag(Integer value, Integer defaultValue) {
        int fallback = defaultValue == null ? 0 : defaultValue;
        if (value == null) {
            return fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
