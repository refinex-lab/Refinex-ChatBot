package cn.refinex.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象转换工具类（基于 Hutool 封装）
 * <p>
 * 关键功能：
 * 1. Map -> Bean（支持忽略大小写、下划线转驼峰、忽略异常等选项）
 * 2. Bean -> Map（支持忽略 null、驼峰/下划线转换、字段过滤）
 * 3. Bean -> Bean（深拷贝/浅拷贝、属性名映射、CopyOptions 支持）
 * 4. 集合转换（copyToList）
 * 5. 深拷贝（通过 Hutool BeanCopier 或 JSON 序列化作为备选）
 * 6. 常见安全 / 容错方法（tryToBean, safeCopy 等）
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanUtils {

    // ========================= 常用快捷方法 =========================

    /**
     * 将 source（支持 Map 或 Bean）转换为目标 Bean 类型（使用默认 CopyOptions）。
     *
     * @param source 源对象（可以是 Map，也可以是 Bean）
     * @param clazz  目标类型
     * @param <T>    目标泛型
     * @return 目标对象，若 source 为 null 则返回 null
     */
    public static <T> T toBean(final Object source, final Class<T> clazz) {
        if (null == source || null == clazz) {
            return null;
        }
        return BeanUtil.toBean(source, clazz);
    }

    /**
     * 将 sourceList 转换为目标对象列表，支持空列表返回空列表。
     *
     * @param sourceList 源对象列表
     * @param clazz      目标类型
     * @param <T1>       源泛型
     * @param <T2>       目标泛型
     * @return 目标对象列表
     */
    public static <T1, T2> List<T2> copyToList(final List<T1> sourceList, final Class<T2> clazz) {
        if (CollectionUtil.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(item -> BeanUtil.toBean(item, clazz))
                .toList();
    }

    /**
     * 将 source 转换为目标 Bean，忽略属性注入错误（用于容忍脏数据场景）。
     *
     * @param source 源对象
     * @param clazz  目标类型
     * @param <T>    目标泛型
     * @return 目标对象
     */
    public static <T> T toBeanIgnoreError(final Object source, final Class<T> clazz) {
        if (null == source || null == clazz) {
            return null;
        }
        return BeanUtil.toBeanIgnoreError(source, clazz);
    }

    /**
     * 将 Map 转换为 Bean（可传 CopyOptions 进行细粒度控制）。
     *
     * @param map         源 Map
     * @param clazz       目标类型
     * @param copyOptions 拷贝选项（可为 null）
     * @param <T>         目标泛型
     * @return 目标对象
     */
    public static <T> T mapToBean(final Map<?, ?> map, final Class<T> clazz, final CopyOptions copyOptions) {
        if (null == map || null == clazz) {
            return null;
        }
        return BeanUtil.toBean(map, clazz, ObjectUtil.defaultIfNull(copyOptions, CopyOptions::create));
    }

    /**
     * 将 Bean 转换为 Map（保留属性顺序）。
     *
     * @param bean              源 Bean
     * @param isToUnderlineCase 是否将字段名转换为下划线模式
     * @param ignoreNullValue   是否忽略值为 null 的属性
     * @return Map（如果 bean 为 null，返回 null）
     */
    public static Map<String, Object> beanToMap(final Object bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        if (null == bean) {
            return Collections.emptyMap();
        }
        return BeanUtil.beanToMap(bean, isToUnderlineCase, ignoreNullValue);
    }

    /**
     * 将 Bean 转换为 Map，并允许传入自定义 CopyOptions（更灵活）。
     *
     * @param bean        源 Bean
     * @param targetMap   目标 Map（如果为 null，会新建 LinkedHashMap）
     * @param copyOptions 拷贝选项
     * @return Map
     */
    public static Map<String, Object> beanToMap(final Object bean, Map<String, Object> targetMap, final CopyOptions copyOptions) {
        if (null == bean) {
            return Collections.emptyMap();
        }
        if (null == targetMap) {
            targetMap = new LinkedHashMap<>();
        }
        return BeanUtil.beanToMap(bean, targetMap, ObjectUtil.defaultIfNull(copyOptions, CopyOptions::create));
    }


    /**
     * 复制属性（支持 CopyOptions），当 source 为 null 时直接返回。常用于 DTO -> Entity、VO -> DTO 等场景。
     *
     * @param source      源对象
     * @param target      目标对象
     * @param copyOptions 复制选项（可为 null）
     */
    public static void copyProperties(final Object source, final Object target, final CopyOptions copyOptions) {
        if (null == source || null == target) {
            return;
        }
        BeanUtil.copyProperties(source, target, ObjectUtil.defaultIfNull(copyOptions, CopyOptions::create));
    }

    /**
     * 简化版 copyProperties：按属性名忽略指定的属性。
     *
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 忽略的属性名
     */
    public static void copyProperties(final Object source, final Object target, final String... ignoreProperties) {
        if (null == source || null == target) {
            return;
        }
        copyProperties(source, target, CopyOptions.create().setIgnoreProperties(ignoreProperties));
    }

    /**
     * 将集合中的对象逐个复制为目标类型并返回 List（支持基本类型转换）。
     *
     * @param collection  原集合
     * @param targetType  目标类型
     * @param copyOptions 复制选项
     * @param <T>         目标泛型
     * @return 目标 List（如果 collection 为 null，返回 null；为空返回空 List）
     */
    public static <T> List<T> copyToList(final Collection<?> collection, final Class<T> targetType, final CopyOptions copyOptions) {
        return BeanUtil.copyToList(collection, targetType, ObjectUtil.defaultIfNull(copyOptions, CopyOptions::create));
    }

    // ========================= 深拷贝 / 克隆 =========================

    /**
     * 使用 BeanCopier 进行属性拷贝（对于同属性名的属性足够快），返回新创建的目标对象。
     * 若 targetClass 有无参构造器则使用无参构造器创建对象。
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>         目标泛型
     * @return 目标对象（如果 source 为 null，返回 null）
     */
    public static <T> T shallowCopy(final Object source, final Class<T> targetClass) {
        if (null == source || null == targetClass) {
            return null;
        }
        final T target = ReflectUtil.newInstanceIfPossible(targetClass);
        BeanCopier.create(source, target, CopyOptions.create()).copy();
        return target;
    }

    /**
     * 尝试深拷贝对象：优先使用 JSON 序列化（兼容复杂对象图），作为备选可改用序列化工具。
     * 注意：使用 JSON 深拷贝时会丢失对象类型的某些特性（例如 transient、不可序列化的字段、类型信息等），仅在 POJO 场景推荐使用。
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>         目标泛型
     * @return 深拷贝后的目标对象，若序列化抛异常则返回 null（或可改为抛出异常）
     */
    public static <T> T deepCopyByJson(final Object source, final Class<T> targetClass) {
        if (null == source) {
            return null;
        }
        try {
            final String json = JSONUtil.toJsonStr(source);
            return JSONUtil.toBean(json, targetClass);
        } catch (JSONException ex) {
            // 遇到不能序列化/反序列化的情况，返回 null（可按需改为抛出运行时异常）
            return null;
        }
    }

    // ========================= 容错 / 安全方法 =========================

    /**
     * 安全的 toBean：解析失败或拷贝失败不抛出异常，返回 Optional.empty()，便于链式处理。
     *
     * @param source 源对象
     * @param clazz  目标类型
     * @param <T>    目标泛型
     * @return Optional
     */
    public static <T> Optional<T> tryToBean(final Object source, final Class<T> clazz) {
        if (null == source || null == clazz) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(toBean(source, clazz));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * 安全的 copyProperties，出现异常时返回 false 并记录（调用方可以自行记录日志）。
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否成功
     */
    public static boolean safeCopy(final Object source, final Object target) {
        if (null == source || null == target) {
            return false;
        }
        try {
            copyProperties(source, target, CopyOptions.create());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // ========================= Map 辅助 =========================


    /**
     * 将 Map 的 key 批量转换为驼峰形式，返回一个新的 Map（不修改原 Map）。
     *
     * @param source 源 Map
     * @return 新 Map（如果 source 为 null 返回 null）
     */
    public static Map<String, Object> mapKeysToCamelCase(final Map<String, Object> source) {
        if (null == source) {
            return Collections.emptyMap();
        }
        if (source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return source.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> toCamelCase(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    /**
     * 转换为驼峰形式
     *
     * @param key 待转换Key
     * @return 驼峰形式Key
     */
    private static String toCamelCase(final String key) {
        if (null == key) {
            return null;
        }
        // 简单实现：下划线转驼峰，首字母小写
        final StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : key.toCharArray()) {
            if (c == '_' || c == '-') {
                upperNext = true;
            } else {
                if (upperNext) {
                    sb.append(Character.toUpperCase(c));
                    upperNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    // ========================= 便捷构建 CopyOptions =========================

    /**
     * 返回一个常用的 CopyOptions：忽略 null、忽略大小写、忽略错误（适用于宽松拷贝）。
     *
     * @return CopyOptions
     */
    public static CopyOptions commonLenientOptions() {
        return CopyOptions.create()
                .setIgnoreNullValue(true)
                .setIgnoreError(true)
                .setIgnoreCase(true);
    }

    /**
     * 返回一个严格的 CopyOptions：不忽略 null、大小写敏感、抛出错误（适用于严格数据迁移）。
     *
     * @return CopyOptions
     */
    public static CopyOptions commonStrictOptions() {
        return CopyOptions.create()
                .setIgnoreNullValue(false)
                .setIgnoreError(false)
                .setIgnoreCase(false);
    }
}
