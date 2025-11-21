package cn.refinex.ai.core.provider.impl;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.AbstractChatModelProvider;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * OpenAI/Azure OpenAI Provider
 * <p>
 * 参考：
 * - <a href="https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html">...</a>
 * - <a href="https://docs.spring.io/spring-ai/reference/api/chat/azure-openai-chat.html">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(OpenAiChatModel.class)
public class OpenAiChatModelProvider extends AbstractChatModelProvider {

    /**
     * 提供器名称
     */
    private static final String PROVIDER_OPENAI = "openai";

    /**
     * Azure OpenAI 提供器名称
     */
    private static final String PROVIDER_AZURE = "azure";

    /**
     * Azure OpenAI 提供器名称
     */
    private static final String PROVIDER_AZURE_OPENAI = "azure-openai";

    /**
     * 默认 OpenAI API 地址
     */
    private static final String DEFAULT_BASE_URL = "https://api.openai.com";

    /**
     * 构造函数
     *
     * @param objectMapper             JSON 映射器
     * @param restClientBuilderProvider Rest 客户端构建器
     * @param webClientBuilderProvider  Web 客户端构建器
     * @param responseErrorHandlerProvider 响应错误处理构建器
     */
    public OpenAiChatModelProvider(ObjectMapper objectMapper,
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
        String providerCode = descriptor.providerCode();
        return StringUtils.equalsAnyIgnoreCase(providerCode, PROVIDER_OPENAI, PROVIDER_AZURE, PROVIDER_AZURE_OPENAI);
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
        OpenAiApi openAiApi = buildOpenAiApi(descriptor);
        OpenAiChatOptions options = buildOptions(descriptor);
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
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
     * 构建 OpenAI API 实例
     *
     * @param descriptor 描述
     * @return API 实例
     */
    private OpenAiApi buildOpenAiApi(ChatModelDescriptor descriptor) {
        String baseUrl = resolveBaseUrl(descriptor, DEFAULT_BASE_URL);
        String apiKey = requireApiKey(descriptor);
        RestClient.Builder restBuilder = restClientBuilder();
        WebClient.Builder webBuilder = webClientBuilder();
        ResponseErrorHandler errorHandler = responseErrorHandler();

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(restBuilder)
                .webClientBuilder(webBuilder)
                .responseErrorHandler(errorHandler)
                .build();
    }

    /**
     * 构建 OpenAI 选项
     *
     * @param descriptor 描述
     * @return 选项
     */
    private OpenAiChatOptions buildOptions(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();

        String modelKey = descriptor.modelKey();
        if (StringUtils.isNotEmpty(modelKey)) {
            builder.model(modelKey);
        } else {
            builder.model(OpenAiApi.DEFAULT_CHAT_MODEL);
        }

        if (agent != null) {
            builder.temperature(toDouble(agent.getTemperature()));
            builder.topP(toDouble(agent.getTopP()));
            builder.presencePenalty(toDouble(agent.getPresencePenalty()));
            builder.frequencyPenalty(toDouble(agent.getFrequencyPenalty()));
            builder.toolChoice(resolveToolChoice(agent));
        }

        Integer maxTokens = resolveMaxTokens(descriptor);
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        List<String> stopSequences = nullIfEmpty(resolveStopSequences(agent));
        if (stopSequences != null) {
            builder.stop(stopSequences);
        }

        return builder.build();
    }
}
