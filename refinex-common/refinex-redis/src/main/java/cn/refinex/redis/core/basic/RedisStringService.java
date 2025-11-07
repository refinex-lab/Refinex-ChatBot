package cn.refinex.redis.core.basic;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis String 类型操作服务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStringService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基本操作 ==========

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置值失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置值并指定过期时间失败，key: {}, value: {}, timeout: {}, unit: {}", key, value, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置值并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     */
    public void set(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception e) {
            log.error("设置值并指定过期时间失败，key: {}, value: {}, duration: {}", key, value, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 不存在时设置值
     *
     * @param key   键
     * @param value 值
     * @return true 设置成功，false key 已存在
     */
    public Boolean setIfAbsent(String key, Object value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            log.error("仅当 key 不存在时设置值失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 不存在时设置值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功，false key 已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("仅当 key 不存在时设置值并指定过期时间失败，key: {}, value: {}, timeout: {}, unit: {}", key, value, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 不存在时设置值并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     * @return true 设置成功，false key 已存在
     */
    public Boolean setIfAbsent(String key, Object value, Duration duration) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, duration);
        } catch (Exception e) {
            log.error("仅当 key 不存在时设置值并指定过期时间失败，key: {}, value: {}, duration: {}", key, value, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 存在时设置值
     *
     * @param key   键
     * @param value 值
     * @return true 设置成功，false key 不存在
     */
    public Boolean setIfPresent(String key, Object value) {
        try {
            return redisTemplate.opsForValue().setIfPresent(key, value);
        } catch (Exception e) {
            log.error("仅当 key 存在时设置值失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 存在时设置值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功，false key 不存在
     */
    public Boolean setIfPresent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfPresent(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("仅当 key 存在时设置值并指定过期时间失败，key: {}, value: {}, timeout: {}, unit: {}", key, value, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当 key 存在时设置值并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     * @return true 设置成功，false key 不存在
     */
    public Boolean setIfPresent(String key, Object value, Duration duration) {
        try {
            return redisTemplate.opsForValue().setIfPresent(key, value, duration);
        } catch (Exception e) {
            log.error("仅当 key 存在时设置值并指定过期时间失败，key: {}, value: {}, duration: {}", key, value, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取值失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取值并转换为指定类型
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   类型参数
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            throw new ClassCastException("无法将值转换为指定类型: " + clazz.getName());
        } catch (Exception e) {
            log.error("获取值并转换类型失败，key: {}, clazz: {}", key, clazz, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取并设置新值（原子操作）
     *
     * @param key   键
     * @param value 新值
     * @return 旧值
     */
    public Object getAndSet(String key, Object value) {
        try {
            return redisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            log.error("获取并设置新值失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 批量操作 ==========

    /**
     * 批量设置值
     *
     * @param map 键值对映射
     */
    public void multiSet(Map<String, Object> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
        } catch (Exception e) {
            log.error("批量设置值失败，map: {}", map, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当所有 key 都不存在时批量设置值
     *
     * @param map 键值对映射
     * @return true 设置成功，false 至少有一个 key 已存在
     */
    public Boolean multiSetIfAbsent(Map<String, Object> map) {
        try {
            return redisTemplate.opsForValue().multiSetIfAbsent(map);
        } catch (Exception e) {
            log.error("仅当所有 key 都不存在时批量设置值失败，map: {}", map, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量获取值
     *
     * @param keys 键集合
     * @return 值列表
     */
    public List<Object> multiGet(Collection<String> keys) {
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.error("批量获取值失败，keys: {}", keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 数值操作 ==========

    /**
     * 递增
     *
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("递增失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递增指定值
     *
     * @param key   键
     * @param delta 递增值
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("递增指定值失败，key: {}, delta: {}", key, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递增指定浮点值
     *
     * @param key   键
     * @param delta 递增值
     * @return 递增后的值
     */
    public Double increment(String key, double delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("递增指定浮点值失败，key: {}, delta: {}", key, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递减
     *
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("递减失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递减指定值
     *
     * @param key   键
     * @param delta 递减值
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("递减指定值失败，key: {}, delta: {}", key, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 字符串操作 ==========

    /**
     * 追加字符串
     *
     * @param key   键
     * @param value 要追加的值
     * @return 追加后字符串的长度
     */
    public Integer append(String key, String value) {
        try {
            return redisTemplate.opsForValue().append(key, value);
        } catch (Exception e) {
            log.error("追加字符串失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取字符串长度
     *
     * @param key 键
     * @return 字符串长度
     */
    public Long size(String key) {
        try {
            return redisTemplate.opsForValue().size(key);
        } catch (Exception e) {
            log.error("获取字符串长度失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取子字符串
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 子字符串
     */
    public String getRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForValue().get(key, start, end);
        } catch (Exception e) {
            log.error("获取子字符串失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置子字符串
     *
     * @param key    键
     * @param value  值
     * @param offset 偏移量
     */
    public void setRange(String key, Object value, long offset) {
        try {
            redisTemplate.opsForValue().set(key, value, offset);
        } catch (Exception e) {
            log.error("设置子字符串失败，key: {}, value: {}, offset: {}", key, value, offset, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 位操作 ==========

    /**
     * 获取位值
     *
     * @param key    键
     * @param offset 偏移量
     * @return 位值
     */
    public Boolean getBit(String key, long offset) {
        try {
            return redisTemplate.opsForValue().getBit(key, offset);
        } catch (Exception e) {
            log.error("获取位值失败，key: {}, offset: {}", key, offset, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置位值
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  位值
     * @return 原位值
     */
    public Boolean setBit(String key, long offset, boolean value) {
        try {
            return redisTemplate.opsForValue().setBit(key, offset, value);
        } catch (Exception e) {
            log.error("设置位值失败，key: {}, offset: {}, value: {}", key, offset, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 统计位值为 1 的数量
     *
     * @param key 键
     * @return 位值为 1 的数量
     */
    public Long bitCount(String key) {
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.stringCommands().bitCount(key.getBytes()));
        } catch (Exception e) {
            log.error("统计位值为 1 的数量失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 统计指定范围内位值为 1 的数量
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 位值为 1 的数量
     */
    public Long bitCount(String key, long start, long end) {
        try {
            return redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.stringCommands().bitCount(key.getBytes(), start, end));
        } catch (Exception e) {
            log.error("统计指定范围内位值为 1 的数量失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
