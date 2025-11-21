package cn.refinex.ai.core.support;

import cn.refinex.ai.core.model.ChatModelDescriptor;

/**
 * 运行时依赖解析器
 *
 * @author Refinex
 * @since 1.0.0
 */
public interface ChatRuntimeSupportResolver {

    /**
     * 根据描述解析运行时依赖
     *
     * @param descriptor 模型描述
     * @return 支撑对象
     */
    ChatRuntimeSupport resolve(ChatModelDescriptor descriptor);
}
