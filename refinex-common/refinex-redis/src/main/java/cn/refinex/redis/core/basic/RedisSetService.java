package cn.refinex.redis.core.basic;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis Set 类型操作服务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSetService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基本操作 ==========

    /**
     * 添加元素到集合
     *
     * @param key    键
     * @param values 值数组
     * @return 添加成功的元素数量
     */
    public Long add(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("添加元素到集合失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从集合中移除元素
     *
     * @param key    键
     * @param values 值数组
     * @return 移除成功的元素数量
     */
    public Long remove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("从集合中移除元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 判断元素是否在集合中
     *
     * @param key   键
     * @param value 值
     * @return true 存在，false 不存在
     */
    public Boolean isMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("判断元素是否在集合中失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量判断元素是否在集合中
     *
     * @param key    键
     * @param values 值数组
     * @return 判断结果映射
     */
    public Map<Object, Boolean> isMember(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().isMember(key, values);
        } catch (Exception e) {
            log.error("批量判断元素是否在集合中失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取集合所有元素
     *
     * @param key 键
     * @return 集合元素
     */
    public Set<Object> members(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取集合所有元素失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long size(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取集合大小失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 随机操作 ==========

    /**
     * 随机获取集合中的一个元素
     *
     * @param key 键
     * @return 随机元素
     */
    public Object randomMember(String key) {
        try {
            return redisTemplate.opsForSet().randomMember(key);
        } catch (Exception e) {
            log.error("随机获取集合中的一个元素失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 随机获取集合中的多个元素
     *
     * @param key   键
     * @param count 数量
     * @return 随机元素列表
     */
    public List<Object> randomMembers(String key, long count) {
        try {
            return redisTemplate.opsForSet().randomMembers(key, count);
        } catch (Exception e) {
            log.error("随机获取集合中的多个元素失败，key: {}, count: {}", key, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 随机获取集合中的多个不重复元素
     *
     * @param key   键
     * @param count 数量
     * @return 随机元素集合
     */
    public Set<Object> distinctRandomMembers(String key, long count) {
        try {
            return redisTemplate.opsForSet().distinctRandomMembers(key, count);
        } catch (Exception e) {
            log.error("随机获取集合中的多个不重复元素失败，key: {}, count: {}", key, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 随机弹出集合中的一个元素
     *
     * @param key 键
     * @return 弹出的元素
     */
    public Object pop(String key) {
        try {
            return redisTemplate.opsForSet().pop(key);
        } catch (Exception e) {
            log.error("随机弹出集合中的一个元素失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 随机弹出集合中的多个元素
     *
     * @param key   键
     * @param count 数量
     * @return 弹出的元素列表
     */
    public List<Object> pop(String key, long count) {
        try {
            return redisTemplate.opsForSet().pop(key, count);
        } catch (Exception e) {
            log.error("随机弹出集合中的多个元素失败，key: {}, count: {}", key, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 移动操作 ==========

    /**
     * 将元素从源集合移动到目标集合
     *
     * @param sourceKey      源键
     * @param value          要移动的值
     * @param destinationKey 目标键
     * @return true 移动成功，false 移动失败
     */
    public Boolean move(String sourceKey, Object value, String destinationKey) {
        try {
            return redisTemplate.opsForSet().move(sourceKey, value, destinationKey);
        } catch (Exception e) {
            log.error("将元素从源集合移动到目标集合失败，sourceKey: {}, value: {}, destinationKey: {}", sourceKey, value, destinationKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 集合运算 ==========

    /**
     * 计算多个集合的交集
     *
     * @param key  键
     * @param keys 其他键
     * @return 交集结果
     */
    public Set<Object> intersect(String key, String... keys) {
        try {
            return redisTemplate.opsForSet().intersect(key, List.of(keys));
        } catch (Exception e) {
            log.error("计算多个集合的交集失败，key: {}, keys: {}", key, keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的交集
     *
     * @param keys 键集合
     * @return 交集结果
     */
    public Set<Object> intersect(Collection<String> keys) {
        try {
            return redisTemplate.opsForSet().intersect(keys);
        } catch (Exception e) {
            log.error("计算多个集合的交集失败，keys: {}", keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的交集并存储到目标集合
     *
     * @param key        键
     * @param otherKeys  其他键
     * @param destKey    目标键
     * @return 交集元素数量
     */
    public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        try {
            return redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("计算多个集合的交集并存储到目标集合失败，key: {}, otherKeys: {}, destKey: {}", key, otherKeys, destKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的并集
     *
     * @param key  键
     * @param keys 其他键
     * @return 并集结果
     */
    public Set<Object> union(String key, String... keys) {
        try {
            return redisTemplate.opsForSet().union(key, List.of(keys));
        } catch (Exception e) {
            log.error("计算多个集合的并集失败，key: {}, keys: {}", key, keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的并集
     *
     * @param keys 键集合
     * @return 并集结果
     */
    public Set<Object> union(Collection<String> keys) {
        try {
            return redisTemplate.opsForSet().union(keys);
        } catch (Exception e) {
            log.error("计算多个集合的并集失败，keys: {}", keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的并集并存储到目标集合
     *
     * @param key       键
     * @param otherKeys 其他键
     * @param destKey   目标键
     * @return 并集元素数量
     */
    public Long unionAndStore(String key, Collection<String> otherKeys, String destKey) {
        try {
            return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("计算多个集合的并集并存储到目标集合失败，key: {}, otherKeys: {}, destKey: {}", key, otherKeys, destKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的差集
     *
     * @param key  键
     * @param keys 其他键
     * @return 差集结果
     */
    public Set<Object> difference(String key, String... keys) {
        try {
            return redisTemplate.opsForSet().difference(key, List.of(keys));
        } catch (Exception e) {
            log.error("计算多个集合的差集失败，key: {}, keys: {}", key, keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的差集
     *
     * @param keys 键集合
     * @return 差集结果
     */
    public Set<Object> difference(Collection<String> keys) {
        try {
            return redisTemplate.opsForSet().difference(keys);
        } catch (Exception e) {
            log.error("计算多个集合的差集失败，keys: {}", keys, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个集合的差集并存储到目标集合
     *
     * @param key       键
     * @param otherKeys 其他键
     * @param destKey   目标键
     * @return 差集元素数量
     */
    public Long differenceAndStore(String key, Collection<String> otherKeys, String destKey) {
        try {
            return redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("计算多个集合的差集并存储到目标集合失败，key: {}, otherKeys: {}, destKey: {}", key, otherKeys, destKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 扫描操作 ==========

    /**
     * 扫描集合元素
     *
     * @param key     键
     * @param options 扫描选项
     * @return 扫描游标
     */
    public Cursor<@NonNull  Object> scan(@NonNull String key, @NonNull ScanOptions options) {
        try {
            return redisTemplate.opsForSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描集合元素失败，key: {}, options: {}", key, options, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的集合元素
     *
     * @param key     键
     * @param pattern 匹配模式
     * @return 扫描游标
     */
    public Cursor<@NonNull Object> scan(@NonNull String key, @NonNull String pattern) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
            return redisTemplate.opsForSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的集合元素失败，key: {}, pattern: {}", key, pattern, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的集合元素（限制数量）
     *
     * @param key     键
     * @param pattern 匹配模式
     * @param count   扫描数量
     * @return 扫描游标
     */
    public Cursor<@NonNull Object> scan(@NonNull String key, @NonNull String pattern, long count) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
            return redisTemplate.opsForSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的集合元素（限制数量）失败，key: {}, pattern: {}, count: {}", key, pattern, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
