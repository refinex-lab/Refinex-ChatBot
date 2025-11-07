package cn.refinex.json.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单且安全的静态 holder，允许你在极端场景下调用 JsonUtils.get("default").toJson(obj)
 * <p>
 * 推荐仅在无法注入 Spring bean（如一些工具类、static 方法）时使用。
 *
 * @author Refinex
 * @since 1.0.0
 */
public final class JsonUtilsHolder {

    /**
     * 存储 JsonUtils 实例的映射，键为实例名称
     */
    private static final ConcurrentHashMap<String, JsonUtils> MAP = new ConcurrentHashMap<>();

    /**
     * 默认的 JsonUtils 实例名称
     */
    public static final String INSTANCE_KEY = "default";

    /**
     * 私有构造函数，防止外部实例化
     */
    private JsonUtilsHolder() {
        // 防止外部实例化
        throw new UnsupportedOperationException("JsonUtilsHolder is a utility class and cannot be instantiated");
    }

    /**
     * 设置 JsonUtils 实例
     *
     * @param key      实例名称
     * @param instance JsonUtils 实例
     */
    public static void set(String key, JsonUtils instance) {
        MAP.putIfAbsent(key, instance);
    }

    /**
     * 获取默认的 JsonUtils 实例
     *
     * @return 默认的 JsonUtils 实例
     */
    public static JsonUtils get() {
        JsonUtils u = MAP.get(INSTANCE_KEY);
        if (u == null) {
            throw new IllegalStateException("JsonUtils not initialized in JsonUtilsHolder. Use DI or ensure JsonUtilsAutoConfiguration runs.");
        }
        return u;
    }

    /**
     * 获取指定名称的 JsonUtils 实例
     *
     * @param key 实例名称
     * @return 指定名称的 JsonUtils 实例
     */
    public static JsonUtils get(String key) {
        JsonUtils u = MAP.get(key);
        if (u == null) {
            throw new IllegalStateException("JsonUtils not found for key: " + key);
        }
        return u;
    }
}
