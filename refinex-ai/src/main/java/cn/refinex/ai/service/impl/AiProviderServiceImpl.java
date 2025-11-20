package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.provider.dto.request.AiProviderCreateRequestDTO;
import cn.refinex.ai.controller.provider.dto.request.AiProviderPageRequest;
import cn.refinex.ai.controller.provider.dto.request.AiProviderUpdateRequestDTO;
import cn.refinex.ai.controller.provider.dto.response.AiProviderResponseDTO;
import cn.refinex.ai.converter.AiProviderConverter;
import cn.refinex.ai.entity.AiProvider;
import cn.refinex.ai.enums.AiProviderType;
import cn.refinex.ai.repository.AiProviderRepository;
import cn.refinex.ai.service.AiProviderService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static cn.refinex.core.util.StringUtils.trimToNull;

/**
 * 模型供应商服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiProviderServiceImpl implements AiProviderService {

    /**
     * 默认供应商类型，默认值为 {@link AiProviderType#PUBLIC}
     */
    private static final AiProviderType DEFAULT_PROVIDER_TYPE = AiProviderType.PUBLIC;

    private final AiProviderRepository repository;
    private final JdbcTemplateManager jdbcManager;
    private final AiProviderConverter converter;

    /**
     * 分页查询模型供应商
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<AiProviderResponseDTO> page(AiProviderPageRequest request) {
        AiProviderPageRequest query = Objects.isNull(request) ? new AiProviderPageRequest() : request;
        String providerType = filterProviderType(query.getProviderType());
        String keyword = trimToNull(query.getKeyword());
        Long userId = LoginHelper.getUserId();
        PageResponse<AiProvider> page = repository.page(providerType, query.getStatus(), keyword, query, userId);
        return page.map(converter::toResponse);
    }

    /**
     * 根据ID查询模型供应商
     *
     * @param id 主键
     * @return 模型供应商
     */
    @Override
    public Optional<AiProviderResponseDTO> findById(Long id) {
        Long userId = LoginHelper.getUserId();
        return repository.findById(id, userId).map(converter::toResponse);
    }

    /**
     * 根据编码查询模型供应商
     *
     * @param code 编码
     * @return 模型供应商
     */
    @Override
    public Optional<AiProviderResponseDTO> findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        Long userId = LoginHelper.getUserId();
        return repository.findByCode(code.trim(), userId).map(converter::toResponse);
    }

    /**
     * 创建模型供应商
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    @Override
    public void create(AiProviderCreateRequestDTO request, Long operatorId) {
        String code = request.providerCode().trim();
        if (repository.findByCode(code, operatorId).isPresent()) {
            throw new BusinessException("供应商编码已存在: " + code);
        }
        AiProvider entity = buildCreateEntity(request, operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(entity, jdbc);
            entity.setId(id);
            return null;
        });
    }

    /**
     * 更新模型供应商
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    @Override
    public void update(Long id, AiProviderUpdateRequestDTO request, Long operatorId) {
        AiProvider exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("供应商不存在或已删除"));
        String newCode = request.providerCode().trim();
        if (!exist.getProviderCode().equals(newCode) && repository.findByCode(newCode, operatorId).isPresent()) {
            throw new BusinessException("供应商编码已存在: " + newCode);
        }

        AiProvider entity = buildUpdateEntity(exist, request, operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(entity, jdbc);
            return null;
        });
    }

    /**
     * 更新状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiProvider exist = repository.findById(id, operatorId)
                .orElseThrow(() -> new BusinessException("供应商不存在或已删除"));
        int normalized = normalizeStatus(status, exist.getStatus() == null ? 1 : exist.getStatus());
        if (exist.getStatus() != null && exist.getStatus() == normalized) {
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
     * 删除模型供应商
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiProvider exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("供应商不存在或已删除"));
        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建创建模型供应商实体
     *
     * @param request    创建请求
     * @param operatorId 操作人
     * @return 模型供应商实体
     */
    private AiProvider buildCreateEntity(AiProviderCreateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiProvider.builder()
                .providerCode(request.providerCode().trim())
                .providerName(request.providerName().trim())
                .providerType(normalizeProviderType(request.providerType()))
                .baseUrl(trimToNull(request.baseUrl()))
                .apiKeyCipher(trimToNull(request.apiKeyCipher()))
                .apiKeyIndex(trimToNull(request.apiKeyIndex()))
                .rateLimitQpm(normalizeRateLimit(request.rateLimitQpm()))
                .status(normalizeStatus(request.status(), 1))
                .remark(trimToNull(request.remark()))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 构建更新模型供应商实体
     *
     * @param exist      存在的模型供应商实体
     * @param request    更新请求
     * @param operatorId 操作人
     * @return 模型供应商实体
     */
    private AiProvider buildUpdateEntity(AiProvider exist, AiProviderUpdateRequestDTO request, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        AiProvider provider = new AiProvider();
        provider.setId(exist.getId());
        provider.setProviderCode(request.providerCode().trim());
        provider.setProviderName(request.providerName().trim());
        provider.setProviderType(resolveProviderType(request.providerType(), exist.getProviderType()));
        provider.setBaseUrl(request.baseUrl() == null ? exist.getBaseUrl() : trimToNull(request.baseUrl()));
        provider.setApiKeyCipher(request.apiKeyCipher() == null ? exist.getApiKeyCipher() : trimToNull(request.apiKeyCipher()));
        provider.setApiKeyIndex(request.apiKeyIndex() == null ? exist.getApiKeyIndex() : trimToNull(request.apiKeyIndex()));
        provider.setRateLimitQpm(request.rateLimitQpm() == null ? exist.getRateLimitQpm() : normalizeRateLimit(request.rateLimitQpm()));
        provider.setStatus(normalizeStatus(request.status(), exist.getStatus() == null ? 1 : exist.getStatus()));
        provider.setRemark(request.remark() == null ? exist.getRemark() : trimToNull(request.remark()));
        provider.setCreateBy(exist.getCreateBy());
        provider.setCreateTime(exist.getCreateTime());
        provider.setUpdateBy(operatorId);
        provider.setUpdateTime(now);
        provider.setDeleted(exist.getDeleted());
        provider.setDeleteBy(exist.getDeleteBy());
        provider.setDeleteTime(exist.getDeleteTime());
        return provider;
    }

    /**
     * 标准化供应商类型
     *
     * @param providerType 供应商类型
     * @return 标准化后的供应商类型
     */
    private String normalizeProviderType(String providerType) {
        return parseProviderTypeOrDefault(providerType).getCode();
    }

    /**
     * 解析供应商类型
     *
     * @param candidate 候选供应商类型
     * @param current   当前供应商类型
     * @return 解析后的供应商类型
     */
    private String resolveProviderType(String candidate, String current) {
        if (StringUtils.hasText(candidate)) {
            return parseProviderType(candidate).getCode();
        }
        if (StringUtils.hasText(current)) {
            return parseProviderType(current).getCode();
        }
        return DEFAULT_PROVIDER_TYPE.getCode();
    }

    /**
     * 标准化状态
     *
     * @param candidate 候选状态
     * @param defaultValue 默认状态
     * @return 标准化后的状态
     */
    private int normalizeStatus(Integer candidate, int defaultValue) {
        if (candidate == null) {
            return defaultValue;
        }
        return candidate != 0 ? 1 : 0;
    }

    /**
     * 标准化速率限制
     *
     * @param rateLimit 速率限制
     * @return 标准化后的速率限制
     */
    private Integer normalizeRateLimit(Integer rateLimit) {
        if (rateLimit == null) {
            return null;
        }
        return Math.max(rateLimit, 0);
    }

    /**
     * 过滤供应商类型
     *
     * @param providerType 供应商类型
     * @return 过滤后的供应商类型或null
     */
    private String filterProviderType(String providerType) {
        if (!StringUtils.hasText(providerType)) {
            return null;
        }
        return parseProviderType(providerType).getCode();
    }

    /**
     * 解析供应商类型或默认值
     *
     * @param raw 原始供应商类型
     * @return 解析后的供应商类型或默认值
     */
    private AiProviderType parseProviderTypeOrDefault(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DEFAULT_PROVIDER_TYPE;
        }
        return parseProviderType(raw);
    }

    /**
     * 解析供应商类型
     *
     * @param raw 原始供应商类型
     * @return 解析后的供应商类型
     */
    private AiProviderType parseProviderType(String raw) {
        try {
            return AiProviderType.fromCode(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("供应商类型不支持: " + raw);
        }
    }
}
