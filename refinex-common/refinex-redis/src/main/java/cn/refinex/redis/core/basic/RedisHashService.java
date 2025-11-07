package cn.refinex.redis.core.basic;

import cn.hutool.json.JSONUtil;
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
 * Redis Hash 类型操作服务
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHashService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基本操作 ==========

    /**
     * 设置 Hash 字段值
     *
     * @param key   Hash 键
     * @param field 字段名
     * @param value 字段值
     */
    public void put(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("设置 Hash 字段值失败，key: {}, field: {}, value: {}", key, field, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 仅当字段不存在时设置值
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     * @return true 设置成功，false 字段已存在
     */
    public Boolean putIfAbsent(String key, String field, Object value) {
        try {
            return redisTemplate.opsForHash().putIfAbsent(key, field, value);
        } catch (Exception e) {
            log.error("仅当字段不存在时设置值失败，key: {}, field: {}, value: {}", key, field, value, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取 Hash 字段值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    public Object get(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("获取 Hash 字段值失败，key: {}, field: {}", key, field, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取 Hash 字段值并转换为指定类型
     *
     * @param key   键
     * @param field 字段
     * @param clazz 目标类型
     * @param <T>   类型参数
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String field, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return JSONUtil.toBean(JSONUtil.toJsonStr(value), clazz);
            }
            throw new ClassCastException("无法将值转换为指定类型: " + clazz.getName());
        } catch (Exception e) {
            log.error("获取 Hash 字段值并转换类型失败，key: {}, field: {}, clazz: {}", key, field, clazz, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除 Hash 字段
     *
     * @param key    键
     * @param fields 字段数组
     * @return 删除的字段数量
     */
    public Long delete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("删除 Hash 字段失败，key: {}, fields: {}", key, fields, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 判断 Hash 字段是否存在
     *
     * @param key   键
     * @param field 字段
     * @return true 存在，false 不存在
     */
    public Boolean hasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("判断 Hash 字段是否存在失败，key: {}, field: {}", key, field, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 批量操作 ==========

    /**
     * 批量设置 Hash 字段值
     *
     * @param key 键
     * @param map 字段值映射
     */
    public void putAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("批量设置 Hash 字段值失败，key: {}, map: {}", key, map, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量获取 Hash 字段值
     *
     * @param key    键
     * @param fields 字段集合
     * @return 值列表
     */
    public List<Object> multiGet(String key, Collection<Object> fields) {
        try {
            return redisTemplate.opsForHash().multiGet(key, fields);
        } catch (Exception e) {
            log.error("批量获取 Hash 字段值失败，key: {}, fields: {}", key, fields, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取所有字段和值
     *
     * @param key 键
     * @return 字段值映射
     */
    public Map<Object, Object> entries(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取所有字段和值失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取所有字段
     *
     * @param key 键
     * @return 字段集合
     */
    public Set<Object> keys(String key) {
        try {
            return redisTemplate.opsForHash().keys(key);
        } catch (Exception e) {
            log.error("获取所有字段失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取所有值
     *
     * @param key 键
     * @return 值列表
     */
    public List<Object> values(String key) {
        try {
            return redisTemplate.opsForHash().values(key);
        } catch (Exception e) {
            log.error("获取所有值失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 数值操作 ==========

    /**
     * 递增 Hash 字段值
     *
     * @param key   键
     * @param field 字段
     * @param delta 递增值
     * @return 递增后的值
     */
    public Long increment(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("递增 Hash 字段值失败，key: {}, field: {}, delta: {}", key, field, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 递增 Hash 字段浮点值
     *
     * @param key   键
     * @param field 字段
     * @param delta 递增值
     * @return 递增后的值
     */
    public Double increment(String key, String field, double delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("递增 Hash 字段浮点值失败，key: {}, field: {}, delta: {}", key, field, delta, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 统计操作 ==========

    /**
     * 获取 Hash 字段数量
     *
     * @param key 键
     * @return 字段数量
     */
    public Long size(String key) {
        try {
            return redisTemplate.opsForHash().size(key);
        } catch (Exception e) {
            log.error("获取 Hash 字段数量失败，key: {}", key, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取 Hash 字段长度
     *
     * @param key   键
     * @param field 字段
     * @return 字段值长度
     */
    public Long lengthOfValue(String key, String field) {
        try {
            return redisTemplate.opsForHash().lengthOfValue(key, field);
        } catch (Exception e) {
            log.error("获取 Hash 字段长度失败，key: {}, field: {}", key, field, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== 扫描操作 ==========

    /**
     * 扫描 Hash 字段
     *
     * @param key     键
     * @param options 扫描选项
     * @return 扫描游标
     */
    public Cursor<Map.Entry<Object, Object>> scan(@NonNull String key, @NonNull ScanOptions options) {
        try {
            return redisTemplate.opsForHash().scan(key, options);
        } catch (Exception e) {
            log.error("扫描 Hash 字段失败，key: {}, options: {}", key, options, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的 Hash 字段
     *
     * @param key     键
     * @param pattern 匹配模式
     * @return 扫描游标
     */
    public Cursor<Map.Entry<Object, Object>> scan(@NonNull String key, @NonNull String pattern) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
            return redisTemplate.opsForHash().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的 Hash 字段失败，key: {}, pattern: {}", key, pattern, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 扫描匹配模式的 Hash 字段（限制数量）
     *
     * @param key     键
     * @param pattern 匹配模式
     * @param count   扫描数量
     * @return 扫描游标
     */
    public Cursor<Map.Entry<Object, Object>> scan(@NonNull String key, @NonNull String pattern, long count) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
            return redisTemplate.opsForHash().scan(key, options);
        } catch (Exception e) {
            log.error("扫描匹配模式的 Hash 字段（限制数量）失败，key: {}, pattern: {}, count: {}", key, pattern, count, e);
            throw new SystemException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
