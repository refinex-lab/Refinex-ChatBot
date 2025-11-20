package cn.refinex.ai.service.impl;

import cn.refinex.ai.controller.model.dto.request.AiModelCreateRequestDTO;
import cn.refinex.ai.controller.model.dto.request.AiModelPageRequest;
import cn.refinex.ai.controller.model.dto.request.AiModelUpdateRequestDTO;
import cn.refinex.ai.controller.model.dto.response.AiModelResponseDTO;
import cn.refinex.ai.converter.AiModelConverter;
import cn.refinex.ai.entity.AiModel;
import cn.refinex.ai.entity.AiProvider;
import cn.refinex.ai.enums.ApiVariant;
import cn.refinex.ai.enums.ModelType;
import cn.refinex.ai.repository.AiModelRepository;
import cn.refinex.ai.repository.AiProviderRepository;
import cn.refinex.ai.service.AiModelService;
import cn.refinex.core.api.PageResponse;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.util.StringUtils;
import cn.refinex.jdbc.core.JdbcTemplateManager;
import cn.refinex.satoken.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * 模型服务实现类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    /**
     * 默认货币，默认值为 {@code USD}
     */
    private static final String DEFAULT_CURRENCY = "USD";

    private final AiModelRepository repository;
    private final AiProviderRepository providerRepository;
    private final JdbcTemplateManager jdbcManager;
    private final AiModelConverter converter;

    /**
     * 分页查询模型
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<AiModelResponseDTO> page(AiModelPageRequest request) {
        AiModelPageRequest query = Objects.isNull(request) ? new AiModelPageRequest() : request;
        String modelType = StringUtils.isNotEmpty(query.getModelType()) ? ModelType.fromCode(query.getModelType()).getCode() : null;
        String apiVariant = StringUtils.isNotEmpty(query.getApiVariant()) ? ApiVariant.fromCode(query.getApiVariant()).getCode() : null;
        String keyword = StringUtils.isNotEmpty(query.getKeyword()) ? query.getKeyword().trim() : null;
        Long userId = LoginHelper.getUserId();
        PageResponse<AiModel> page = repository.page(query.getProviderId(), modelType, apiVariant, query.getStatus(), keyword, query, userId);
        return page.map(converter::toResponse);
    }

    /**
     * 根据ID查询模型
     *
     * @param id 主键
     * @return 模型
     */
    @Override
    public Optional<AiModelResponseDTO> findById(Long id) {
        Long userId = LoginHelper.getUserId();
        return repository.findById(id, userId).map(converter::toResponse);
    }

    /**
     * 创建模型
     *
     * @param request    创建请求
     * @param operatorId 操作人
     */
    @Override
    public void create(AiModelCreateRequestDTO request, Long operatorId) {
        AiProvider provider = providerRepository.findById(request.providerId(), operatorId)
                .orElseThrow(() -> new BusinessException("供应商不存在或已删除"));
        String key = request.modelKey().trim();
        if (repository.findByProviderAndKey(provider.getId(), key, operatorId).isPresent()) {
            throw new BusinessException("模型已存在: " + key);
        }
        AiModel entity = buildCreateEntity(request, provider.getId(), operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            long id = repository.insert(entity, jdbc);
            entity.setId(id);
            return null;
        });
    }

    /**
     * 更新模型
     *
     * @param id         主键
     * @param request    更新请求
     * @param operatorId 操作人
     */
    @Override
    public void update(Long id, AiModelUpdateRequestDTO request, Long operatorId) {
        AiModel exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("模型不存在或已删除"));
        AiProvider provider = providerRepository.findById(request.providerId(), operatorId)
                .orElseThrow(() -> new BusinessException("供应商不存在或已删除"));
        String newKey = request.modelKey().trim();
        if (!exist.getModelKey().equals(newKey) || !exist.getProviderId().equals(provider.getId())) {
            repository.findByProviderAndKey(provider.getId(), newKey, operatorId)
                    .filter(model -> !model.getId().equals(exist.getId()))
                    .ifPresent(model -> {
                        throw new BusinessException("模型已存在: " + newKey);
                    });
        }
        AiModel entity = buildUpdateEntity(exist, request, provider.getId(), operatorId);
        jdbcManager.executeInTransaction(jdbc -> {
            repository.update(entity, jdbc);
            return null;
        });
    }

    /**
     * 更新模型状态
     *
     * @param id         主键
     * @param status     状态
     * @param operatorId 操作人
     */
    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        AiModel exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("模型不存在或已删除"));
        int normalized = normalizeFlag(status, valueOrDefault(exist.getStatus(), 1));
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
     * 删除模型
     *
     * @param id         主键
     * @param operatorId 操作人
     */
    @Override
    public void delete(Long id, Long operatorId) {
        AiModel exist = repository.findById(id, operatorId).orElseThrow(() -> new BusinessException("模型不存在或已删除"));
        jdbcManager.executeInTransaction(jdbc -> {
            repository.logicalDelete(exist.getId(), operatorId, jdbc);
            return null;
        });
    }

    /**
     * 构建创建实体
     *
     * @param request    创建请求
     * @param providerId 供应商ID
     * @param operatorId 操作人
     * @return 实体
     */
    private AiModel buildCreateEntity(AiModelCreateRequestDTO request, Long providerId, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        return AiModel.builder()
                .providerId(providerId)
                .modelKey(request.modelKey().trim())
                .modelName(StringUtils.trimToNull(request.modelName()))
                .modelType(normalizeModelType(request.modelType(), null))
                .apiVariant(normalizeApiVariant(request.apiVariant(), null))
                .region(StringUtils.trimToNull(request.region()))
                .contextWindowTokens(normalizePositive(request.contextWindowTokens()))
                .maxOutputTokens(normalizePositive(request.maxOutputTokens()))
                .priceInputPer1k(request.priceInputPer1k())
                .priceOutputPer1k(request.priceOutputPer1k())
                .currency(normalizeCurrency(request.currency()))
                .supportToolCall(normalizeFlag(request.supportToolCall(), 0))
                .supportVision(normalizeFlag(request.supportVision(), 0))
                .supportAudioIn(normalizeFlag(request.supportAudioIn(), 0))
                .supportAudioOut(normalizeFlag(request.supportAudioOut(), 0))
                .supportStructuredOut(normalizeFlag(request.supportStructuredOut(), 0))
                .status(normalizeFlag(request.status(), 1))
                .remark(StringUtils.trimToNull(request.remark()))
                .createBy(operatorId)
                .createTime(now)
                .updateBy(operatorId)
                .updateTime(now)
                .deleted(0)
                .build();
    }

    /**
     * 构建更新实体
     *
     * @param exist      存在的实体
     * @param request    更新请求
     * @param providerId 供应商ID
     * @param operatorId 操作人
     * @return 实体
     */
    private AiModel buildUpdateEntity(AiModel exist, AiModelUpdateRequestDTO request, Long providerId, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        AiModel model = new AiModel();
        model.setId(exist.getId());
        model.setProviderId(providerId);
        model.setModelKey(request.modelKey().trim());
        model.setModelName(request.modelName() == null ? exist.getModelName() : StringUtils.trimToNull(request.modelName()));
        model.setModelType(normalizeModelType(request.modelType(), exist.getModelType()));
        model.setApiVariant(normalizeApiVariant(request.apiVariant(), exist.getApiVariant()));
        model.setRegion(request.region() == null ? exist.getRegion() : StringUtils.trimToNull(request.region()));
        model.setContextWindowTokens(request.contextWindowTokens() == null ? exist.getContextWindowTokens() : normalizePositive(request.contextWindowTokens()));
        model.setMaxOutputTokens(request.maxOutputTokens() == null ? exist.getMaxOutputTokens() : normalizePositive(request.maxOutputTokens()));
        model.setPriceInputPer1k(request.priceInputPer1k() == null ? exist.getPriceInputPer1k() : request.priceInputPer1k());
        model.setPriceOutputPer1k(request.priceOutputPer1k() == null ? exist.getPriceOutputPer1k() : request.priceOutputPer1k());
        model.setCurrency(request.currency() == null ? exist.getCurrency() : normalizeCurrency(request.currency()));
        model.setSupportToolCall(normalizeFlag(request.supportToolCall(), valueOrDefault(exist.getSupportToolCall(), 0)));
        model.setSupportVision(normalizeFlag(request.supportVision(), valueOrDefault(exist.getSupportVision(), 0)));
        model.setSupportAudioIn(normalizeFlag(request.supportAudioIn(), valueOrDefault(exist.getSupportAudioIn(), 0)));
        model.setSupportAudioOut(normalizeFlag(request.supportAudioOut(), valueOrDefault(exist.getSupportAudioOut(), 0)));
        model.setSupportStructuredOut(normalizeFlag(request.supportStructuredOut(), valueOrDefault(exist.getSupportStructuredOut(), 0)));
        model.setStatus(normalizeFlag(request.status(), valueOrDefault(exist.getStatus(), 1)));
        model.setRemark(request.remark() == null ? exist.getRemark() : StringUtils.trimToNull(request.remark()));
        model.setCreateBy(exist.getCreateBy());
        model.setCreateTime(exist.getCreateTime());
        model.setUpdateBy(operatorId);
        model.setUpdateTime(now);
        model.setDeleted(exist.getDeleted());
        model.setDeleteBy(exist.getDeleteBy());
        model.setDeleteTime(exist.getDeleteTime());
        return model;
    }

    /**
     * 归一化模型类型
     *
     * @param candidate 候选值
     * @param fallback  备用值
     * @return 归一化后的模型类型
     */
    private String normalizeModelType(String candidate, String fallback) {
        if (StringUtils.isNotEmpty(candidate)) {
            return ModelType.fromCode(candidate).getCode();
        }
        if (StringUtils.isNotEmpty(fallback)) {
            return ModelType.fromCode(fallback).getCode();
        }
        throw new BusinessException("模型类型不能为空");
    }

    /**
     * 归一化API变体
     *
     * @param candidate 候选值
     * @param fallback  备用值
     * @return 归一化后的API变体
     */
    private String normalizeApiVariant(String candidate, String fallback) {
        if (candidate == null) {
            return fallback;
        }
        if (!StringUtils.isNotEmpty(candidate)) {
            return null;
        }
        return ApiVariant.fromCode(candidate).getCode();
    }

    /**
     * 归一化货币
     *
     * @param currency 货币
     * @return 归一化后的货币
     */
    private String normalizeCurrency(String currency) {
        if (!StringUtils.isNotEmpty(currency)) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 归一化非负整数
     *
     * @param value 值
     * @return 归一化后的非负整数
     */
    private Integer normalizePositive(Integer value) {
        if (value == null) {
            return null;
        }
        return Math.max(value, 0);
    }

    /**
     * 归一化标志位
     *
     * @param candidate 候选值
     * @param defaultValue 默认值
     * @return 归一化后的标志位
     */
    private int normalizeFlag(Integer candidate, int defaultValue) {
        if (candidate == null) {
            return defaultValue;
        }
        return candidate != 0 ? 1 : 0;
    }

    /**
     * 获取非空值或默认值
     *
     * @param value       值
     * @param defaultValue 默认值
     * @return 非空值或默认值
     */
    private int valueOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

}
