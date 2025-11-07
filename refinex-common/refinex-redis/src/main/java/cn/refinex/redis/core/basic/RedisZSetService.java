package cn.refinex.redis.core.basic;

import cn.refinex.core.api.ApiStatus;
import cn.refinex.core.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * Redis ZSet（有序集合）类型操作服务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisZSetService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基本操作 ==========

    /**
     * 添加元素到有序集合
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return true 添加成功，false 元素已存在且分数被更新
     */
    public Boolean add(String key, Object value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("添加元素到有序集合失败，key: {}, value: {}, score: {}", key, value, score, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量添加元素到有序集合
     *
     * @param key    键
     * @param tuples 元素分数对集合
     * @return 添加成功的元素数量
     */
    public Long add(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
        try {
            return redisTemplate.opsForZSet().add(key, tuples);
        } catch (Exception e) {
            log.error("批量添加元素到有序集合失败，key: {}, tuples: {}", key, tuples, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从有序集合中移除元素
     *
     * @param key    键
     * @param values 值数组
     * @return 移除成功的元素数量
     */
    public Long remove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            log.error("从有序集合中移除元素失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递增元素的分数
     *
     * @param key   键
     * @param value 值
     * @param delta 递增值
     * @return 递增后的分数
     */
    public Double incrementScore(String key, Object value, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            log.error("递增元素的分数失败，key: {}, value: {}, delta: {}", key, value, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取元素的分数
     *
     * @param key   键
     * @param value 值
     * @return 分数
     */
    public Double score(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            log.error("获取元素的分数失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量获取元素的分数
     *
     * @param key    键
     * @param values 值数组
     * @return 分数列表
     */
    public java.util.List<Double> score(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().score(key, values);
        } catch (Exception e) {
            log.error("批量获取元素的分数失败，key: {}, values: {}", key, values, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 排名操作 ==========

    /**
     * 获取元素的排名（从小到大）
     *
     * @param key   键
     * @param value 值
     * @return 排名（从0开始）
     */
    public Long rank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().rank(key, value);
        } catch (Exception e) {
            log.error("获取元素的排名（从小到大）失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取元素的排名（从大到小）
     *
     * @param key   键
     * @param value 值
     * @return 排名（从0开始）
     */
    public Long reverseRank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().reverseRank(key, value);
        } catch (Exception e) {
            log.error("获取元素的排名（从大到小）失败，key: {}, value: {}", key, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 范围查询 ==========

    /**
     * 根据排名范围获取元素（从小到大）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素集合
     */
    public Set<Object> range(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error("根据排名范围获取元素（从小到大）失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据排名范围获取元素和分数（从小到大）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素分数对集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> rangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        } catch (Exception e) {
            log.error("根据排名范围获取元素和分数（从小到大）失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据排名范围获取元素（从大到小）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素集合
     */
    public Set<Object> reverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("根据排名范围获取元素（从大到小）失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据排名范围获取元素和分数（从大到小）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素分数对集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        } catch (Exception e) {
            log.error("根据排名范围获取元素和分数（从大到小）失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<Object> rangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("根据分数范围获取元素失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取元素和分数
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素分数对集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
        } catch (Exception e) {
            log.error("根据分数范围获取元素和分数失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取限定数量的元素
     *
     * @param key    键
     * @param min    最小分数
     * @param max    最大分数
     * @param offset 偏移量
     * @param count  数量
     * @return 元素集合
     */
    public Set<Object> rangeByScore(String key, double min, double max, long offset, long count) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
        } catch (Exception e) {
            log.error("根据分数范围获取限定数量的元素失败，key: {}, min: {}, max: {}, offset: {}, count: {}", key, min, max, offset, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取限定数量的元素和分数
     *
     * @param key    键
     * @param min    最小分数
     * @param max    最大分数
     * @param offset 偏移量
     * @param count  数量
     * @return 元素分数对集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        try {
            return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
        } catch (Exception e) {
            log.error("根据分数范围获取限定数量的元素和分数失败，key: {}, min: {}, max: {}, offset: {}, count: {}", key, min, max, offset, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取元素（从大到小）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<Object> reverseRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("根据分数范围获取元素（从大到小）失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取元素和分数（从大到小）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素分数对集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeByScoreWithScores(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
        } catch (Exception e) {
            log.error("根据分数范围获取元素和分数（从大到小）失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围获取限定数量的元素（从大到小）
     *
     * @param key    键
     * @param min    最小分数
     * @param max    最大分数
     * @param offset 偏移量
     * @param count  数量
     * @return 元素集合
     */
    public Set<Object> reverseRangeByScore(String key, double min, double max, long offset, long count) {
        try {
            return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
        } catch (Exception e) {
            log.error("根据分数范围获取限定数量的元素（从大到小）失败，key: {}, min: {}, max: {}, offset: {}, count: {}", key, min, max, offset, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 统计和删除操作 ==========

    /**
     * 统计分数范围内的元素数量
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    public Long count(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().count(key, min, max);
        } catch (Exception e) {
            log.error("统计分数范围内的元素数量失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取有序集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long size(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            log.error("获取有序集合大小失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取有序集合大小（别名）
     *
     * @param key 键
     * @return 集合大小
     */
    public Long zCard(String key) {
        try {
            return redisTemplate.opsForZSet().zCard(key);
        } catch (Exception e) {
            log.error("获取有序集合大小失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据排名范围删除元素
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 删除的元素数量
     */
    public Long removeRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().removeRange(key, start, end);
        } catch (Exception e) {
            log.error("根据排名范围删除元素失败，key: {}, start: {}, end: {}", key, start, end, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据分数范围删除元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 删除的元素数量
     */
    public Long removeRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("根据分数范围删除元素失败，key: {}, min: {}, max: {}", key, min, max, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 集合运算 ==========

    /**
     * 计算多个有序集合的交集并存储到目标集合
     *
     * @param key       键
     * @param otherKeys 其他键
     * @param destKey   目标键
     * @return 交集元素数量
     */
    public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        try {
            return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("计算多个有序集合的交集并存储到目标集合失败，key: {}, otherKeys: {}, destKey: {}", key, otherKeys, destKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 计算多个有序集合的并集并存储到目标集合
     *
     * @param key       键
     * @param otherKeys 其他键
     * @param destKey   目标键
     * @return 并集元素数量
     */
    public Long unionAndStore(String key, Collection<String> otherKeys, String destKey) {
        try {
            return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("计算多个有序集合的并集并存储到目标集合失败，key: {}, otherKeys: {}, destKey: {}", key, otherKeys, destKey, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 扫描操作 ==========

    /**
     * 扫描有序集合元素
     *
     * @param key     键
     * @param options 扫描选项
     * @return 扫描游标
     */
    public Cursor<ZSetOperations.TypedTuple<Object>> scan(@NonNull String key, @NonNull ScanOptions options) {
        try {
            return redisTemplate.opsForZSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描有序集合元素失败，key: {}, options: {}", key, options, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的有序集合元素
     *
     * @param key     键
     * @param pattern 匹配模式
     * @return 扫描游标
     */
    public Cursor<ZSetOperations.TypedTuple<Object>> scan(@NonNull String key, @NonNull String pattern) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
            return redisTemplate.opsForZSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的有序集合元素失败，key: {}, pattern: {}", key, pattern, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的有序集合元素（限制数量）
     *
     * @param key     键
     * @param pattern 匹配模式
     * @param count   扫描数量
     * @return 扫描游标
     */
    public Cursor<ZSetOperations.TypedTuple<Object>> scan(@NonNull String key, @NonNull String pattern, long count) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
            return redisTemplate.opsForZSet().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的有序集合元素（限制数量）失败，key: {}, pattern: {}, count: {}", key, pattern, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
