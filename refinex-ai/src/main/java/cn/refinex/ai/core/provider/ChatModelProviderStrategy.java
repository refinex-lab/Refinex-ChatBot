package cn.refinex.ai.core.provider;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.support.ChatRuntimeSupport;

/**
 * ChatModel 提供方策略
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ChatModelProviderStrategy {

    /**
     * 是否支持当前描述
     *
     * @param descriptor 描述
     * @return 支持标记
     */
    boolean supports(ChatModelDescriptor descriptor);

    /**
     * 创建上下文
     *
     * @param descriptor      描述
     * @param runtimeSupport  依赖
     * @return 上下文
     */
    ChatModelContext create(ChatModelDescriptor descriptor, ChatRuntimeSupport runtimeSupport);
}
