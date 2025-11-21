package cn.refinex.ai.core.factory;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;

/**
 * ChatModel 构建入口
 * <p>
 * 参考：<a href="https://docs.spring.io/spring-ai/reference/api/chatmodel.html">...</a>
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ChatModelFactory {

    /**
     * 根据描述构建运行上下文
     *
     * @param descriptor 模型描述
     * @return 上下文
     */
    ChatModelContext create(ChatModelDescriptor descriptor);
}
