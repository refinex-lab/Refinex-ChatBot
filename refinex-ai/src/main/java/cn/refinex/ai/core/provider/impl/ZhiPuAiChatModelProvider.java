package cn.refinex.ai.core.provider.impl;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.AbstractChatModelProvider;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.core.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 智谱清言 Chat Provider
 * <p>
 * 参考：<a href="https://docs.spring.io/spring-ai/reference/api/chat/zhipuai-chat.html">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(ZhiPuAiChatModel.class)
public class ZhiPuAiChatModelProvider extends AbstractChatModelProvider {

    /**
     * 智谱清言 Provider 编码
     */
    private static final String PROVIDER = "zhipuai";

    /**
     * 默认基础 URL
     */
    private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";

    /**
     * 默认模型
     */
    private static final String DEFAULT_MODEL = "glm-4-air";

    /**
     * 构造函数
     *
     * @param objectMapper            JSON 映射器
     * @param restClientBuilderProvider  REST 客户端构建器提供器
     * @param webClientBuilderProvider   Web 客户端构建器提供器
     * @param responseErrorHandlerProvider 响应错误处理提供器
     */
    public ZhiPuAiChatModelProvider(ObjectMapper objectMapper,
                                    ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                    ObjectProvider<org.springframework.web.reactive.function.client.WebClient.Builder> webClientBuilderProvider,
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
        ZhiPuAiApi api = new ZhiPuAiApi(requireApiKey(descriptor),
                resolveBaseUrl(descriptor, DEFAULT_BASE_URL),
                restClientBuilder(),
                responseErrorHandler());

        ZhiPuAiChatOptions options = buildOptions(descriptor);
        ZhiPuAiChatModel chatModel = new ZhiPuAiChatModel(api, options,
                runtimeSupport.getToolCallingManager(),
                runtimeSupport.getRetryTemplate(),
                runtimeSupport.getObservationRegistry());

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
     * 构建 OpenAI 选项
     *
     * @param descriptor 描述
     * @return 选项
     */
    private ZhiPuAiChatOptions buildOptions(ChatModelDescriptor descriptor) {
        AiAgent agent = agent(descriptor);
        ZhiPuAiChatOptions.Builder builder = ZhiPuAiChatOptions.builder()
                .model(StringUtils.blankToDefault(descriptor.modelKey(), DEFAULT_MODEL));

        if (agent != null) {
            if (agent.getTemperature() != null) {
                builder.temperature(toDouble(agent.getTemperature()));
            }
            if (agent.getTopP() != null) {
                builder.topP(toDouble(agent.getTopP()));
            }
            List<String> stop = nullIfEmpty(resolveStopSequences(agent));
            if (stop != null) {
                builder.stop(stop);
            }
            Object toolChoice = resolveToolChoice(agent);
            if (toolChoice != null) {
                builder.toolChoice(toJson(toolChoice));
            }
        }

        Integer maxTokens = resolveMaxTokens(descriptor);
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }
}
