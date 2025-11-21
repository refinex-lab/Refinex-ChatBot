package cn.refinex.ai.core.factory;

import cn.refinex.ai.core.model.ChatModelContext;
import cn.refinex.ai.core.model.ChatModelDescriptor;
import cn.refinex.ai.core.provider.ChatModelProviderStrategy;
import cn.refinex.ai.core.support.ChatRuntimeSupport;
import cn.refinex.ai.core.support.ChatRuntimeSupportResolver;
import cn.refinex.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认工厂实现
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultChatModelFactory implements ChatModelFactory {

    private final List<ChatModelProviderStrategy> providerStrategies;
    private final ChatRuntimeSupportResolver runtimeSupportResolver;

    /**
     * 根据描述构建运行上下文
     *
     * @param descriptor 模型描述
     * @return 上下文
     */
    @Override
    public ChatModelContext create(ChatModelDescriptor descriptor) {
        if (descriptor == null) {
            throw new BusinessException("ChatModelDescriptor 不能为空");
        }

        ChatModelProviderStrategy strategy = providerStrategies.stream()
                .sorted(OrderComparator.INSTANCE)
                .filter(provider -> provider.supports(descriptor))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到可处理的 ChatModelProvider, provider: " + descriptor.providerCode()));

        ChatRuntimeSupport runtimeSupport = runtimeSupportResolver.resolve(descriptor);
        ChatModelContext context = strategy.create(descriptor, runtimeSupport);
        log.debug("创建 ChatModelContext 成功, provider={}, model={}", descriptor.providerCode(), descriptor.modelKey());
        return context;
    }
}
