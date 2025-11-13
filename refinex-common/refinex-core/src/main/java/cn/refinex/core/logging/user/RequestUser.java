package cn.refinex.core.logging.user;

import org.jspecify.annotations.Nullable;

/**
 * 请求用户信息
 *
 * @author Refinex
 * @since 1.0.0
 */
public record RequestUser(@Nullable Long userId, @Nullable String username) {
}
