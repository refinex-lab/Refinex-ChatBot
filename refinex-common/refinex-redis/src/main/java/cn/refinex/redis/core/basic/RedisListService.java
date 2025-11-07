package cn.refinex.redis.core.basic;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis List 类型操作服务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisListService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 左端操作 ==========

    /**
     * 从左端推入元素
     *
     * @param key   键
     * @param value 值
     * @return 推入后列表长度
     */
    public Long leftPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("从左端推入元素失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从左端批量推入元素
     *
     * @param key    键
     * @param values 值数组
     * @return 推入后列表长度
     */
    public Long leftPushAll(String key, Object... values) {
        try {
            return redisTemplate.opsForList().leftPushAll(key, values);
        } catch (Exception e) {
            log.error("从左端批量推入元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从左端批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 推入后列表长度
     */
    public Long leftPushAll(String key, Collection<Object> values) {
        try {
            return redisTemplate.opsForList().leftPushAll(key, values);
        } catch (Exception e) {
            log.error("从左端批量推入元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当列表存在时从左端推入元素
     *
     * @param key   键
     * @param value 值
     * @return 推入后列表长度
     */
    public Long leftPushIfPresent(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPushIfPresent(key, value);
        } catch (Exception e) {
            log.error("仅当列表存在时从左端推入元素失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 在指定元素前推入元素
     *
     * @param key   键
     * @param pivot 基准元素
     * @param value 要推入的值
     * @return 推入后列表长度
     */
    public Long leftPush(String key, Object pivot, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, pivot, value);
        } catch (Exception e) {
            log.error("在指定元素前推入元素失败，key: {}, pivot: {}, value: {}", key, pivot, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从左端弹出元素
     *
     * @param key 键
     * @return 弹出的元素
     */
    public Object leftPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("从左端弹出元素失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从左端弹出指定数量的元素
     *
     * @param key   键
     * @param count 数量
     * @return 弹出的元素列表
     */
    public List<Object> leftPop(String key, long count) {
        try {
            return redisTemplate.opsForList().leftPop(key, count);
        } catch (Exception e) {
            log.error("从左端弹出指定数量的元素失败，key: {}, count: {}", key, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从左端弹出元素
     *
     * @param key     键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 弹出的元素
     */
    public Object leftPop(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForList().leftPop(key, timeout, unit);
        } catch (Exception e) {
            log.error("阻塞式从左端弹出元素失败，key: {}, timeout: {}, unit: {}", key, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从左端弹出元素
     *
     * @param key      键
     * @param duration 超时时间
     * @return 弹出的元素
     */
    public Object leftPop(String key, Duration duration) {
        try {
            return redisTemplate.opsForList().leftPop(key, duration);
        } catch (Exception e) {
            log.error("阻塞式从左端弹出元素失败，key: {}, duration: {}", key, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 右端操作 ==========

    /**
     * 从右端推入元素
     *
     * @param key   键
     * @param value 值
     * @return 推入后列表长度
     */
    public Long rightPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("从右端推入元素失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从右端批量推入元素
     *
     * @param key    键
     * @param values 值数组
     * @return 推入后列表长度
     */
    public Long rightPushAll(String key, Object... values) {
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("从右端批量推入元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从右端批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 推入后列表长度
     */
    public Long rightPushAll(String key, Collection<Object> values) {
        try {
            return redisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            log.error("从右端批量推入元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当列表存在时从右端推入元素
     *
     * @param key   键
     * @param value 值
     * @return 推入后列表长度
     */
    public Long rightPushIfPresent(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPushIfPresent(key, value);
        } catch (Exception e) {
            log.error("仅当列表存在时从右端推入元素失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 在指定元素后推入元素
     *
     * @param key   键
     * @param pivot 基准元素
     * @param value 要推入的值
     * @return 推入后列表长度
     */
    public Long rightPush(String key, Object pivot, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, pivot, value);
        } catch (Exception e) {
            log.error("在指定元素后推入元素失败，key: {}, pivot: {}, value: {}", key, pivot, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从右端弹出元素
     *
     * @param key 键
     * @return 弹出的元素
     */
    public Object rightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("从右端弹出元素失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从右端弹出指定数量的元素
     *
     * @param key   键
     * @param count 数量
     * @return 弹出的元素列表
     */
    public List<Object> rightPop(String key, long count) {
        try {
            return redisTemplate.opsForList().rightPop(key, count);
        } catch (Exception e) {
            log.error("从右端弹出指定数量的元素失败，key: {}, count: {}", key, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从右端弹出元素
     *
     * @param key     键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 弹出的元素
     */
    public Object rightPop(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForList().rightPop(key, timeout, unit);
        } catch (Exception e) {
            log.error("阻塞式从右端弹出元素失败，key: {}, timeout: {}, unit: {}", key, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从右端弹出元素
     *
     * @param key      键
     * @param duration 超时时间
     * @return 弹出的元素
     */
    public Object rightPop(String key, Duration duration) {
        try {
            return redisTemplate.opsForList().rightPop(key, duration);
        } catch (Exception e) {
            log.error("阻塞式从右端弹出元素失败，key: {}, duration: {}", key, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 移动操作 ==========

    /**
     * 从源列表右端弹出元素并推入目标列表左端
     *
     * @param sourceKey      源键
     * @param destinationKey 目标键
     * @return 移动的元素
     */
    public Object rightPopAndLeftPush(String sourceKey, String destinationKey) {
        try {
            return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
        } catch (Exception e) {
            log.error("从源列表右端弹出元素并推入目标列表左端失败，sourceKey: {}, destinationKey: {}", sourceKey, destinationKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从源列表右端弹出元素并推入目标列表左端
     *
     * @param sourceKey      源键
     * @param destinationKey 目标键
     * @param timeout        超时时间
     * @param unit           时间单位
     * @return 移动的元素
     */
    public Object rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
        } catch (Exception e) {
            log.error("阻塞式从源列表右端弹出元素并推入目标列表左端失败，sourceKey: {}, destinationKey: {}, timeout: {}, unit: {}", sourceKey, destinationKey, timeout, unit, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 阻塞式从源列表右端弹出元素并推入目标列表左端
     *
     * @param sourceKey      源键
     * @param destinationKey 目标键
     * @param duration       超时时间
     * @return 移动的元素
     */
    public Object rightPopAndLeftPush(String sourceKey, String destinationKey, Duration duration) {
        try {
            return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, duration);
        } catch (Exception e) {
            log.error("阻塞式从源列表右端弹出元素并推入目标列表左端失败，sourceKey: {}, destinationKey: {}, duration: {}", sourceKey, destinationKey, duration, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 索引操作 ==========

    /**
     * 根据索引获取元素
     *
     * @param key   键
     * @param index 索引
     * @return 元素
     */
    public Object index(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("根据索引获取元素失败，key: {}, index: {}", key, index, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据索引设置元素
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void set(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
        } catch (Exception e) {
            log.error("根据索引设置元素失败，key: {}, index: {}, value: {}", key, index, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 范围操作 ==========

    /**
     * 获取指定范围的元素
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public List<Object> range(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取指定范围的元素失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 修剪列表，只保留指定范围的元素
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引
     */
    public void trim(String key, long start, long end) {
        try {
            redisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("修剪列表失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 移除操作 ==========

    /**
     * 移除列表中的元素
     *
     * @param key   键
     * @param count 移除数量（正数从头开始，负数从尾开始，0移除所有）
     * @param value 要移除的值
     * @return 实际移除的数量
     */
    public Long remove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("移除列表中的元素失败，key: {}, count: {}, value: {}", key, count, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 统计操作 ==========

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    public Long size(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取列表长度失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
