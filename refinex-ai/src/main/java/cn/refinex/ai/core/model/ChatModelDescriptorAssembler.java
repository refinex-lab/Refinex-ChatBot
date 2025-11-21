package cn.refinex.ai.core.model;

import cn.refinex.ai.core.support.ProviderSecretResolver;
import cn.refinex.ai.entity.AiAgent;
import cn.refinex.ai.entity.AiModel;
import cn.refinex.ai.entity.AiProvider;
import cn.refinex.ai.repository.AiAgentRepository;
import cn.refinex.ai.repository.AiModelRepository;
import cn.refinex.ai.repository.AiProviderRepository;
import cn.refinex.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ChatModelDescriptor 构建器
 *
 * @author Refinex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ChatModelDescriptorAssembler {

    private final AiAgentRepository agentRepository;
    private final AiModelRepository modelRepository;
    private final AiProviderRepository providerRepository;
    private final ProviderSecretResolver providerSecretResolver;

    /**
     * 根据 AgentId 构建描述
     *
     * @param agentId  Agent 主键
     * @param operator 当前用户
     * @return 描述
     */
    public ChatModelDescriptor build(Long agentId, Long operator) {
        AiAgent agent = agentRepository
                .findById(agentId, operator)
                .orElseThrow(() -> new BusinessException("Agent 不存在或已删除"));
        return build(agent, operator);
    }

    /**
     * 根据 Agent 构建描述
     *
     * @param agent    Agent
     * @param operator 当前用户
     * @return 描述
     */
    public ChatModelDescriptor build(AiAgent agent, Long operator) {
        AiModel model = modelRepository
                .findById(agent.getModelId(), operator)
                .orElseThrow(() -> new BusinessException("关联模型不存在或已删除"));

        AiProvider provider = providerRepository
                .findById(model.getProviderId(), operator)
                .orElseThrow(() -> new BusinessException("关联供应商不存在或已删除"));

        return build(agent, model, provider);
    }

    /**
     * 聚合已有实体
     *
     * @param agent    Agent
     * @param model    模型
     * @param provider 供应商
     * @return 描述
     */
    public ChatModelDescriptor build(AiAgent agent, AiModel model, AiProvider provider) {
        String apiKey = providerSecretResolver.resolveApiKey(provider);
        return ChatModelDescriptor.builder()
                .agent(agent)
                .model(model)
                .provider(provider)
                .apiKey(apiKey)
                .build();
    }
}
