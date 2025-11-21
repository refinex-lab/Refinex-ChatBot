package cn.refinex.ai.core.model;

import io.micrometer.observation.ObservationRegistry;
import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;

/**
 * ChatModel 工厂产出的运行时上下文
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@Builder
public class ChatModelContext {

    /**
     * 真实的 ChatModel 实例
     */
    private final ChatModel chatModel;

    /**
     * 默认的模型 Options
     */
    private final ChatOptions chatOptions;

    /**
     * 描述信息，方便上层透传
     */
    private final ChatModelDescriptor descriptor;

    /**
     * 工具调用管理器
     */
    private final ToolCallingManager toolCallingManager;

    /**
     * 重试策略
     */
    private final RetryTemplate retryTemplate;

    /**
     * 观测注册器
     */
    private final ObservationRegistry observationRegistry;
}
