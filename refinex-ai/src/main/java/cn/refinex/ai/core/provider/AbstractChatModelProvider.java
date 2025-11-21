package cn.refinex.ai.core.provider;

import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.ai.entity.AiModel;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 提供通用辅助方法
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChatModelProvider implements ChatModelProviderStrategy {

    /**
     * 字符串列表类型引用
     */
    private static final TypeReference<List<String>> STR_LIST = new TypeReference<>() {
    };

    /**
     * 对象映射类型引用
     */
    private static final TypeReference<Map<String, Object>> OBJ_MAP = new TypeReference<>() {
    };

    protected final ObjectMapper objectMapper;
    protected final ObjectProvider<RestClient.Builder> restClientBuilderProvider;
    protected final ObjectProvider<WebClient.Builder> webClientBuilderProvider;
    protected final ObjectProvider<ResponseErrorHandler> responseErrorHandlerProvider;

    /**
     * 解析 REST 客户端构建器
     *
     * @return REST 客户端构建器
     */
    protected RestClient.Builder restClientBuilder() {
        RestClient.Builder builder = restClientBuilderProvider.getIfAvailable();
        return builder != null ? builder : RestClient.builder();
    }

    /**
     * 解析 Web 客户端构建器
     *
     * @return Web 客户端构建器
     */
    protected WebClient.Builder webClientBuilder() {
        WebClient.Builder builder = webClientBuilderProvider.getIfAvailable();
        return builder != null ? builder : WebClient.builder();
    }

    /**
     * 解析响应错误处理程序
     *
     * @return 响应错误处理程序
     */
    protected ResponseErrorHandler responseErrorHandler() {
        ResponseErrorHandler handler = responseErrorHandlerProvider.getIfAvailable();
        return handler != null ? handler : new DefaultResponseErrorHandler();
    }

    /**
     * 解析代理
     *
     * @param descriptor 模型描述
     * @return 代理
     */
    protected AiAgent agent(ChatModelDescriptor descriptor) {
        return descriptor.getAgent();
    }

    /**
     * 解析模型
     *
     * @param descriptor 模型描述
     * @return 模型
     */
    protected AiModel model(ChatModelDescriptor descriptor) {
        return descriptor.getModel();
    }

    /**
     * 转换为双精度浮点数
     *
     * @param value 大十进制数
     * @return 双精度浮点数
     */
    protected Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    /**
     * 归一化正整数
     *
     * @param value 整数
     * @return 归一化后的整数
     */
    protected Integer normalizePositive(Integer value) {
        return value != null && value > 0 ? value : null;
    }

    /**
     * 解析停止词序列
     *
     * @param agent 代理
     * @return 停止词序列
     */
    protected List<String> resolveStopSequences(AiAgent agent) {
        if (agent == null || StringUtils.isEmpty(agent.getStopSequences())) {
            return Collections.emptyList();
        }

        String raw = agent.getStopSequences().trim();
        try {
            if (raw.startsWith("[")) {
                return objectMapper.readValue(raw, STR_LIST);
            }

            String[] parts = raw.split(",");
            return java.util.Arrays.stream(parts)
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .toList();
        } catch (Exception ex) {
            log.warn("解析停止词失败: {}", raw, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 解析最大令牌数
     *
     * @param descriptor 模型描述
     * @return 最大令牌数
     */
    protected Integer resolveMaxTokens(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        if (agent != null && agent.getMaxTokens() != null && agent.getMaxTokens() > 0) {
            return agent.getMaxTokens();
        }

        AiModel model = model(descriptor);
        if (model != null && model.getMaxOutputTokens() != null && model.getMaxOutputTokens() > 0) {
            return model.getMaxOutputTokens();
        }
        return null;
    }

    /**
     * 解析基础 URL
     *
     * @param descriptor 模型描述
     * @param defaultUrl 默认 URL
     * @return 基础 URL
     */
    protected String resolveBaseUrl(ChatModelDescriptor descriptor, String defaultUrl) {
        String baseUrl = descriptor.providerBaseUrl();
        return StringUtils.isEmpty(baseUrl) ? defaultUrl : baseUrl.trim();
    }

    /**
     * 要求 API Key
     *
     * @param descriptor 模型描述
     * @return API Key
     */
    protected String requireApiKey(ChatModelDescriptor descriptor) {
        String apiKey = descriptor.getApiKey();
        if (StringUtils.isEmpty(apiKey)) {
            throw new BusinessException("Provider[" + descriptor.providerCode() + "] 缺少 API Key");
        }
        return apiKey;
    }

    /**
     * 如果列表为空则返回 null
     *
     * @param values 字符串列表
     * @return 列表或 null
     */
    protected List<String> nullIfEmpty(List<String> values) {
        return values == null || values.isEmpty() ? null : values;
    }

    /**
     * 解析工具选择
     *
     * @param agent 代理
     * @return 工具选择
     */
    protected Object resolveToolChoice(AiAgent agent) {
        if (agent == null || StringUtils.isEmpty(agent.getToolChoice())) {
            return null;
        }

        String raw = agent.getToolChoice().trim();
        if (raw.startsWith("{")) {
            try {
                return objectMapper.readValue(raw, OBJ_MAP);
            } catch (Exception ex) {
                log.warn("解析 toolChoice 失败: {}", raw, ex);
            }
        }
        return raw;
    }

    /**
     * 确保运行时依赖不为空
     *
     * @param runtimeSupport 运行时依赖
     */
    protected void ensureRuntime(ChatRuntimeSupport runtimeSupport) {
        Objects.requireNonNull(runtimeSupport, "ChatRuntimeSupport 不能为空");
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param value 对象
     * @return JSON 字符串
     */
    protected String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.warn("序列化 JSON 失败: {}", value, ex);
            return value.toString();
        }
    }
}
