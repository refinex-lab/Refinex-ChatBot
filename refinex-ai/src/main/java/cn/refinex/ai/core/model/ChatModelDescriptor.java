package cn.refinex.ai.core.model;

import cn.refinex.ai.entity.AiAgent;
import cn.refinex.ai.entity.AiModel;
import cn.refinex.ai.entity.AiProvider;
import cn.refinex.ai.enums.ApiVariant;
import cn.refinex.ai.enums.ModelType;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 组合了 Provider/Model/Agent 信息的运行时模型描述
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@Builder
public class ChatModelDescriptor {

    /**
     * 模型供应商
     */
    private final AiProvider provider;

    /**
     * 模型实体
     */
    private final AiModel model;

    /**
     * 触发当前调用的 Agent
     */
    private final AiAgent agent;

    /**
     * 已解密的 API Key
     */
    private final String apiKey;

    /**
     * 补充属性，供厂商私有参数使用
     */
    @Singular("attribute")
    private final Map<String, Object> attributes;

    /**
     * 获取 Provider Code
     *
     * @return code
     */
    public String providerCode() {
        return Optional.ofNullable(provider)
                .map(AiProvider::getProviderCode)
                .orElse(null);
    }

    /**
     * 获取供应商基础地址
     *
     * @return base url
     */
    public String providerBaseUrl() {
        return Optional.ofNullable(provider)
                .map(AiProvider::getBaseUrl)
                .orElse(null);
    }

    /**
     * 获取模型键
     *
     * @return key
     */
    public String modelKey() {
        return Optional.ofNullable(model)
                .map(AiModel::getModelKey)
                .orElse(null);
    }

    /**
     * 模型类型
     *
     * @return 模型类型枚举
     */
    public Optional<ModelType> modelType() {
        return Optional.ofNullable(model)
                .map(AiModel::getModelType)
                .filter(StringUtils::isNotEmpty)
                .map(ModelType::fromCode);
    }

    /**
     * API 形态
     *
     * @return 枚举
     */
    public Optional<ApiVariant> apiVariant() {
        return Optional.ofNullable(model)
                .map(AiModel::getApiVariant)
                .filter(StringUtils::isNotEmpty)
                .map(ApiVariant::fromCode);
    }

    /**
     * 获取自定义属性
     *
     * @param name 属性名
     * @param type 返回类型
     * @param <T>  类型
     * @return Optional
     */
    public <T> Optional<T> attribute(String name, Class<T> type) {
        if (StringUtils.isEmpty(name) || type == null) {
            return Optional.empty();
        }
        Object value = Optional.ofNullable(attributes).orElse(Collections.emptyMap()).get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new BusinessException("ChatModelDescriptor attribute 类型不匹配: " + name);
        }
        return Optional.of(type.cast(value));
    }

    /**
     * 只读属性
     *
     * @return map
     */
    public Map<String, Object> getAttributes() {
        return Optional.ofNullable(attributes).map(Collections::unmodifiableMap).orElseGet(Collections::emptyMap);
    }
}
