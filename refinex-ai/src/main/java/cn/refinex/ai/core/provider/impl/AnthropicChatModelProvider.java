package cn.refinex.ai.core.provider.impl;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.AbstractChatModelProvider;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Anthropic 模型提供方
 * <p>
 * 参考：<a href="https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(AnthropicChatModel.class)
public class AnthropicChatModelProvider extends AbstractChatModelProvider {

    /**
     * 模型提供方代码
     */
    private static final String PROVIDER = "anthropic";

    /**
     * 构造函数
     *
     * @param objectMapper                 对象映射器
     * @param restClientBuilderProvider    REST 客户端构建器提供器
     * @param webClientBuilderProvider     Web 客户端构建器提供器
     * @param responseErrorHandlerProvider 响应错误处理提供器
     */
    public AnthropicChatModelProvider(ObjectMapper objectMapper,
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
        return StringUtils.equalsIgnoreCase(PROVIDER, descriptor.providerCode());
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
        AnthropicApi api = buildAnthropicApi(descriptor);
        AnthropicChatOptions options = buildOptions(descriptor);
        AnthropicChatModel chatModel = AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(options)
                .retryTemplate(runtimeSupport.getRetryTemplate())
                .toolCallingManager(runtimeSupport.getToolCallingManager())
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
     * 构建 Anthropic API
     *
     * @param descriptor 描述
     * @return API
     */
    private AnthropicApi buildAnthropicApi(ChatModelDescriptor descriptor) {
        String baseUrl = resolveBaseUrl(descriptor, AnthropicApi.DEFAULT_BASE_URL);
        String apiKey = requireApiKey(descriptor);
        return AnthropicApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restClientBuilder())
                .webClientBuilder(webClientBuilder())
                .responseErrorHandler(responseErrorHandler())
                .build();
    }

    /**
     * 构建选项
     *
     * @param descriptor 描述
     * @return 选项
     */
    private AnthropicChatOptions buildOptions(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        AnthropicChatOptions.Builder builder = AnthropicChatOptions.builder()
                .model(StringUtils.blankToDefault(descriptor.modelKey(), AnthropicChatModel.DEFAULT_MODEL_NAME));

        if (agent != null) {
            if (agent.getTemperature() != null) {
                builder.temperature(toDouble(agent.getTemperature()));
            }
            if (agent.getTopP() != null) {
                builder.topP(toDouble(agent.getTopP()));
            }
            List<String> stop = nullIfEmpty(resolveStopSequences(agent));
            if (stop != null) {
                builder.stopSequences(stop);
            }
        }

        Integer maxTokens = resolveMaxTokens(descriptor);
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }
}
