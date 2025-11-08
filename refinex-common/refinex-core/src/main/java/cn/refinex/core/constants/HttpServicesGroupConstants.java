package cn.refinex.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
