package cn.refinex.ai.core.provider.impl;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.AbstractChatModelProvider;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.core.util.StringUtils;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * DashScope(Qianwen) Provider
 * <p>
 * 参考：<a href="https://java2ai.com/">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(DashScopeChatModel.class)
public class DashScopeChatModelProvider extends AbstractChatModelProvider {

    /**
     * 提供器名称
     */
    private static final String PROVIDER = "dashscope";

    /**
     * Qwen 模型提供器
     */
    private static final String PROVIDER_QWEN = "qwen";

    /**
     * 默认基础 URL
     */
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

    /**
     * 默认模型
     */
    private static final String DEFAULT_MODEL = "qwen-plus";

    /**
     * 构造函数
     *
     * @param objectMapper                 对象映射器
     * @param restClientBuilderProvider    REST 客户端构建器提供器
     * @param webClientBuilderProvider     Web 客户端构建器提供器
     * @param responseErrorHandlerProvider 响应错误处理提供器
     */
    public DashScopeChatModelProvider(ObjectMapper objectMapper,
                                      ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                      ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                                      ObjectProvider<ResponseErrorHandler> responseErrorHandlerProvider) {
        super(objectMapper, restClientBuilderProvider, webClientBuilderProvider, responseErrorHandlerProvider);
    }

    /**
     * 是否支持当前描述
     *
     * @param descriptor 描述
     * @return 支持标记
     */
    @Override
    public boolean supports(ChatModelDescriptor descriptor) {
        return StringUtils.equalsAnyIgnoreCase(descriptor.providerCode(), PROVIDER, PROVIDER_QWEN);
    }

    /**
     * 创建上下文
     *
     * @param descriptor      描述
     * @param runtimeSupport  依赖
     * @return 上下文
     */
    @Override
    public ChatModelContext create(ChatModelDescriptor descriptor, ChatRuntimeSupport runtimeSupport) {
        ensureRuntime(runtimeSupport);
        DashScopeApi api = buildDashScopeApi(descriptor);
        DashScopeChatOptions options = buildOptions(descriptor);
        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(api)
                .defaultOptions(options)
                .toolCallingManager(runtimeSupport.getToolCallingManager())
                .retryTemplate(runtimeSupport.getRetryTemplate())
                .observationRegistry(runtimeSupport.getObservationRegistry())
                .build();

        return ChatModelContext.builder()
                .chatModel(chatModel)
                .chatOptions(options)
                .descriptor(descriptor)
                .toolCallingManager(runtimeSupport.getToolCallingManager())
                .retryTemplate(runtimeSupport.getRetryTemplate())
                .observationRegistry(runtimeSupport.getObservationRegistry())
                .build();
    }

    /**
     * 构建 DashScope API
     *
     * @param descriptor 描述
     * @return API 实例
     */
    private DashScopeApi buildDashScopeApi(ChatModelDescriptor descriptor) {
        return DashScopeApi.builder()
                .baseUrl(resolveBaseUrl(descriptor, DEFAULT_BASE_URL))
                .apiKey(requireApiKey(descriptor))
                .restClientBuilder(restClientBuilder())
                .webClientBuilder(webClientBuilder())
                .responseErrorHandler(responseErrorHandler())
                .build();
    }

    /**
     * 构建 Chat Options
     *
     * @param descriptor 描述
     * @return Options 实例
     */
    private DashScopeChatOptions buildOptions(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        DashScopeChatOptions.DashscopeChatOptionsBuilder builder = DashScopeChatOptions.builder()
                .withModel(StringUtils.blankToDefault(descriptor.modelKey(), DEFAULT_MODEL));

        if (agent != null) {
            if (agent.getTemperature() != null) {
                builder.withTemperature(toDouble(agent.getTemperature()));
            }
            if (agent.getTopP() != null) {
                builder.withTopP(toDouble(agent.getTopP()));
            }
            if (agent.getFrequencyPenalty() != null) {
                builder.withRepetitionPenalty(toDouble(agent.getFrequencyPenalty()));
            }
            List<String> stop = nullIfEmpty(resolveStopSequences(agent));
            if (stop != null) {
                builder.withStop(List.copyOf(stop));
            }
            Object toolChoice = resolveToolChoice(agent);
            if (toolChoice != null) {
                builder.withToolChoice(toolChoice);
            }
        }

        Integer maxTokens = resolveMaxTokens(descriptor);
        if (maxTokens != null) {
            builder.withMaxToken(maxTokens);
        }

        return builder.build();
    }
}
