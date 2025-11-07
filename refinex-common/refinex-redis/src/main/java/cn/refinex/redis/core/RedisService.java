package cn.refinex.redis.core;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.SystemException;
import cn.refinex.redis.core.basic.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 统一服务入口
 * <p>
 * 提供 Redis 各种数据类型的操作服务，作为统一的访问入口。
 * 集成了 String、Hash、List、Set、ZSet 等数据类型的专业化服务。
 * </p>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisStringService stringService;
    private final RedisHashService hashService;
    private final RedisListService listService;
    private final RedisSetService setService;
    private final RedisZSetService zSetService;

    // ========== 通用操作 ==========

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true 存在，false 不存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断 key 是否存在失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除 key
     *
     * @param key 键
     * @return true 删除成功，false 删除失败
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除 key 失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量删除 key
     *
     * @param keys 键集合
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("批量删除 key 失败，keys: {}", keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功，false 设置失败
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (key == null) {
                log.error("设置 key 过期时间失败，key 为 null, timeout: {}, unit: {}", timeout, unit);
                throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
            }
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置 key 过期时间失败，key: {}, timeout: {}, unit: {}", key, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key      键
     * @param duration 过期时间
     * @return true 设置成功，false 设置失败
     */
    public Boolean expire(String key, Duration duration) {
        try {
            if (key == null) {
                log.error("设置 key 过期时间失败，key 为 null, duration: {}", duration);
                throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
            }
            return redisTemplate.expire(key, duration);
        } catch (Exception e) {
            log.error("设置 key 过期时间失败，key: {}, duration: {}", key, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取 key 的过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1 表示永不过期，-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        try {
            if (key == null) {
                // 返回 -2 表示 key 不存在，避免上层空指针或异常中断流程
                log.warn("获取 key 过期时间时 key 为 null，按不存在处理并返回 -2");
                return -2L;
            }
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("获取 key 过期时间失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取 key 的过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 过期时间，-1 表示永不过期，-2 表示 key 不存在
     */
    public Long getExpire(String key, TimeUnit unit) {
        try {
            if (key == null) {
                // 返回 -2 表示 key 不存在，避免上层空指针或异常中断流程
                log.warn("获取 key 过期时间时 key 为 null, unit: {}，按不存在处理并返回 -2", unit);
                return -2L;
            }
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("获取 key 过期时间失败，key: {}, unit: {}", key, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 移除 key 的过期时间，使其永不过期
     *
     * @param key 键
     * @return true 移除成功，false 移除失败
     */
    public Boolean persist(String key) {
        try {
            return redisTemplate.persist(key);
        } catch (Exception e) {
            log.error("移除 key 过期时间失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 查找匹配的 key
     *
     * @param pattern 匹配模式
     * @return 匹配的 key 集合
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("查找匹配的 key 失败，pattern: {}", pattern, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 数据类型专业化服务访问器 ==========

    /**
     * 获取 String 类型操作服务
     *
     * @return RedisStringService
     */
    public RedisStringService string() {
        return stringService;
    }

    /**
     * 获取 Hash 类型操作服务
     *
     * @return RedisHashService
     */
    public RedisHashService hash() {
        return hashService;
    }

    /**
     * 获取 List 类型操作服务
     *
     * @return RedisListService
     */
    public RedisListService list() {
        return listService;
    }

    /**
     * 获取 Set 类型操作服务
     *
     * @return RedisSetService
     */
    public RedisSetService set() {
        return setService;
    }

    /**
     * 获取 ZSet 类型操作服务
     *
     * @return RedisZSetService
     */
    public RedisZSetService zSet() {
        return zSetService;
    }

}
