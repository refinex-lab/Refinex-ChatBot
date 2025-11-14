package cn.refinex.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Http 服务分组常量
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpServicesGroupConstants {

    /**
     * Refinex 平台服务分组
     */
    public static final String REFINEX_PLATFORM_GROUP = "refinex-platform";

    /**
     * Refinex 知识库服务分组
     */
    public static final String REFINEX_KB_GROUP = "refinex-kb";

    /**
     * Refinex 智能体服务分组
     */
    public static final String REFINEX_AI_GROUP = "refinex-ai";

    /**
     * 所有已知的服务分组名称
     * - 用于需要遍历全部服务前缀（例如网关白名单扩展、跨模块通用逻辑）时统一获取
     * - 后续新增/调整服务分组，只需在此常量中维护即可
     */
    public static final List<String> ALL_GROUPS = List.of(
            REFINEX_PLATFORM_GROUP,
            REFINEX_KB_GROUP,
            REFINEX_AI_GROUP
    );
}
