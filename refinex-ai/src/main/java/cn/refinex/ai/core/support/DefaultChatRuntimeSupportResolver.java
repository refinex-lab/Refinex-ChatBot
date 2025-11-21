package cn.refinex.ai.core.support;

import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.ai.entity.AiModel;
import cn.refinex.core.exception.BusinessException;
import cn.refinex.core.util.StringUtils;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 默认运行时依赖解析器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultChatRuntimeSupportResolver implements ChatRuntimeSupportResolver {

    private final ObjectProvider<ToolCallingManager> toolCallingManagerProvider;
    private final ObjectProvider<RetryTemplate> retryTemplateProvider;
    private final ObjectProvider<ObservationRegistry> observationRegistryProvider;

    /**
     * 根据描述解析运行时依赖
     *
     * @param descriptor 模型描述
     * @return 支撑对象
     */
    @Override
    public ChatRuntimeSupport resolve(ChatModelDescriptor descriptor) {
        if (descriptor == null) {
            throw new BusinessException("ChatModelDescriptor 不能为空");
        }

        ToolCallingManager toolCallingManager = resolveToolCallingManager(descriptor);
        RetryTemplate retryTemplate = resolveRetryTemplate();
        ObservationRegistry observationRegistry = resolveObservationRegistry();
        return ChatRuntimeSupport.builder()
                .toolCallingManager(toolCallingManager)
                .retryTemplate(retryTemplate)
                .observationRegistry(observationRegistry)
                .build();
    }

    /**
     * 解析工具调用管理器
     *
     * @param descriptor 模型描述
     * @return 工具调用管理器
     */
    private ToolCallingManager resolveToolCallingManager(ChatModelDescriptor descriptor) {
        if (!shouldEnableTools(descriptor)) {
            return null;
        }

        ToolCallingManager manager = toolCallingManagerProvider.getIfAvailable();
        if (manager == null) {
            log.warn("当前 Agent [{}] 开启了工具调用，但未发现 ToolCallingManager Bean",
                    descriptor.getAgent() == null ? "UNKNOWN" : descriptor.getAgent().getAgentCode());
        }
        return manager;
    }

    /**
     * 是否开启工具调用
     *
     * @param descriptor 模型描述
     * @return 开启标记
     */
    private boolean shouldEnableTools(ChatModelDescriptor descriptor) {
        AiAgent agent = descriptor.getAgent();
        if (agent != null) {
            String choice = StringUtils.trimToNull(agent.getToolChoice());
            if (choice == null) {
                return isModelSupportTool(descriptor.getModel());
            }
            return !"none".equalsIgnoreCase(choice);
        }
        return isModelSupportTool(descriptor.getModel());
    }

    /**
     * 是否模型支持工具调用
     *
     * @param model 模型
     * @return 支持标记
     */
    private boolean isModelSupportTool(AiModel model) {
        return model != null && Objects.equals(model.getSupportToolCall(), 1);
    }

    /**
     * 解析重试模板
     *
     * @return 重试模板
     */
    private RetryTemplate resolveRetryTemplate() {
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable();
        if (retryTemplate == null) {
            retryTemplate = RetryTemplate.defaultInstance();
        }
        return retryTemplate;
    }

    /**
     * 解析观测注册器
     *
     * @return 观测注册器
     */
    private ObservationRegistry resolveObservationRegistry() {
        ObservationRegistry registry = observationRegistryProvider.getIfAvailable();
        if (registry == null) {
            registry = ObservationRegistry.NOOP;
        }
        return registry;
    }
}
