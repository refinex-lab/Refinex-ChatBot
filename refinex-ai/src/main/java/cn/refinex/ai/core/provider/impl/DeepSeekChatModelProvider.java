package cn.refinex.ai.core.provider.impl;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.AbstractChatModelProvider;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * DeepSeek Chat Provider
 * <p>
 * 参考：<a href="https://docs.spring.io/spring-ai/reference/api/chat/deepseek-chat.html">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(DeepSeekChatModel.class)
public class DeepSeekChatModelProvider extends AbstractChatModelProvider {

    /**
     * 提供器名称
     */
    private static final String PROVIDER = "deepseek";

    /**
     * 默认基础 URL
     */
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";

    /**
     * 默认模型
     */
    private static final String DEFAULT_MODEL = "deepseek-chat";

    /**
     * 构造函数
     *
     * @param objectMapper                 对象映射器
     * @param restClientBuilderProvider    REST 客户端构建器提供器
     * @param webClientBuilderProvider     Web 客户端构建器提供器
     * @param responseErrorHandlerProvider 响应错误处理提供器
     */
    public DeepSeekChatModelProvider(ObjectMapper objectMapper,
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
        DeepSeekApi api = buildDeepSeekApi(descriptor);
        DeepSeekChatOptions options = buildOptions(descriptor);
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(api)
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
     * 构建 DeepSeek API
     *
     * @param descriptor 描述
     * @return API 实例
     */
    private DeepSeekApi buildDeepSeekApi(ChatModelDescriptor descriptor) {
        return DeepSeekApi.builder()
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
     * @return 选项实例
     */
    private DeepSeekChatOptions buildOptions(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        DeepSeekChatOptions.Builder builder = DeepSeekChatOptions.builder()
                .model(StringUtils.blankToDefault(descriptor.modelKey(), DEFAULT_MODEL));

        if (agent != null) {
            if (agent.getTemperature() != null) {
                builder.temperature(toDouble(agent.getTemperature()));
            }
            if (agent.getTopP() != null) {
                builder.topP(toDouble(agent.getTopP()));
            }
            if (agent.getPresencePenalty() != null) {
                builder.presencePenalty(toDouble(agent.getPresencePenalty()));
            }
            if (agent.getFrequencyPenalty() != null) {
                builder.frequencyPenalty(toDouble(agent.getFrequencyPenalty()));
            }
            List<String> stop = nullIfEmpty(resolveStopSequences(agent));
            if (stop != null) {
                builder.stop(stop);
            }
            Object toolChoice = resolveToolChoice(agent);
            if (toolChoice != null) {
                builder.toolChoice(toolChoice);
            }
        }

        Integer maxTokens = resolveMaxTokens(descriptor);
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }
}
