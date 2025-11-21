package cn.refinex.ai.core.support;

import io.micrometer.observation.ObservationRegistry;
import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;

/**
 * 封装运行 ChatModel 所需的通用组件
 *
 * @author Refinex
 * @since 1.0.0
 */
@Getter
@Builder
public class ChatRuntimeSupport {

    /**
     * 工具调用管理器
     */
    private final ToolCallingManager toolCallingManager;

    /**
     * 重试模板
     */
    private final RetryTemplate retryTemplate;

    /**
     * 观测注册器
     */
    private final ObservationRegistry observationRegistry;
}
