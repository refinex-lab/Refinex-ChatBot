package cn.refinex.core.util;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 反射工具类
 * <p>
 * 主要功能模块：
 * <ul>
 *   <li>类操作：加载类、获取类信息、判断类关系</li>
 *   <li>实例创建：创建对象实例、支持多种构造方式</li>
 *   <li>方法操作：调用方法、获取方法信息、方法查找</li>
 *   <li>字段操作：获取/设置字段值、字段查找</li>
 *   <li>注解处理：获取注解、判断注解存在</li>
 *   <li>泛型处理：获取泛型类型、泛型参数</li>
 *   <li>属性拷贝：对象属性复制、深拷贝</li>
 * </ul>
 *
 * @author Refinex
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectUtils {

    /**
     * 方法缓存
     */
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>(256);

    /**
     * 字段缓存
     */
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>(256);

    /**
     * 类缓存
     */
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 缓存最大容量
     */
    private static final int MAX_CACHE_SIZE = 500;

    // ==================== 类操作 ====================

    /**
     * 加载类
     * <p>
     * 使用当前线程的类加载器加载类。
     * </p>
     *
     * @param className 类的全限定名
     * @return 类对象，加载失败返回null
     * @throws IllegalArgumentException 如果类名为空
     */
    public static Class<?> loadClass(String className) {
        return loadClass(className, true);
    }

    /**
     * 加载类
     *
     * @param className  类的全限定名
     * @param initialize 是否初始化类
     * @return 类对象，加载失败返回null
     * @throws IllegalArgumentException 如果类名为空
     */
    public static Class<?> loadClass(String className, boolean initialize) {
        validateNotBlank(className, "类名不能为空");

        // 从缓存中获取
        Class<?> cachedClass = CLASS_CACHE.get(className);
        if (cachedClass != null) {
            return cachedClass;
        }

        try {
            Class<?> clazz = ClassUtil.loadClass(className, initialize);
            if (clazz != null) {
                cacheClass(className, clazz);
            }
            return clazz;
        } catch (Exception e) {
            log.error("加载类失败: {}", className, e);
            return null;
        }
    }

    /**
     * 判断类是否存在
     *
     * @param className 类的全限定名
     * @return 类是否存在
     * @throws IllegalArgumentException 如果类名为空
     */
    public static boolean classExists(String className) {
        validateNotBlank(className, "类名不能为空");
        return loadClass(className, false) != null;
    }

    /**
     * 获取类的简单名称
     *
     * @param clazz 类对象
     * @return 简单名称
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static String getSimpleName(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return clazz.getSimpleName();
    }

    /**
     * 获取类的包名
     *
     * @param clazz 类对象
     * @return 包名
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static String getPackageName(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        Package pkg = clazz.getPackage();
        return pkg != null ? pkg.getName() : "";
    }

    /**
     * 判断类是否为接口
     *
     * @param clazz 类对象
     * @return 是否为接口
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isInterface(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return clazz.isInterface();
    }

    /**
     * 判断类是否为抽象类
     *
     * @param clazz 类对象
     * @return 是否为抽象类
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isAbstract(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 判断类是否为枚举
     *
     * @param clazz 类对象
     * @return 是否为枚举
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isEnum(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return clazz.isEnum();
    }

    /**
     * 判断类是否为数组
     *
     * @param clazz 类对象
     * @return 是否为数组
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isArray(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return clazz.isArray();
    }

    /**
     * 判断类是否为基本类型
     *
     * @param clazz 类对象
     * @return 是否为基本类型
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isPrimitive(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return clazz.isPrimitive();
    }

    /**
     * 判断是否为基本类型或包装类型
     *
     * @param clazz 类对象
     * @return 是否为基本类型或包装类型
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return ClassUtil.isBasicType(clazz);
    }

    /**
     * 判断子类是否为父类的子类或实现类
     *
     * @param subClass   子类
     * @param superClass 父类
     * @return 是否为子类或实现类
     * @throws IllegalArgumentException 如果任一类对象为null
     */
    public static boolean isSubclass(Class<?> subClass, Class<?> superClass) {
        validateNotNull(subClass, "子类对象不能为null");
        validateNotNull(superClass, "父类对象不能为null");
        return superClass.isAssignableFrom(subClass);
    }

    /**
     * 获取类的所有父类
     *
     * @param clazz 类对象
     * @return 所有父类列表（不包括Object）
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Class<?>> getSuperclasses(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        List<Class<?>> superclasses = new ArrayList<>();
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            superclasses.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return superclasses;
    }

    /**
     * 获取类的所有接口
     *
     * @param clazz 类对象
     * @return 所有接口列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Class<?>> getInterfaces(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(clazz.getInterfaces());
    }

    /**
     * 获取类的所有接口（包括父类的接口）
     *
     * @param clazz 类对象
     * @return 所有接口列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        Set<Class<?>> interfaces = new LinkedHashSet<>();

        while (clazz != null) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }

        return new ArrayList<>(interfaces);
    }

    // ==================== 实例创建 ====================

    /**
     * 创建对象实例（使用无参构造函数）
     *
     * @param clazz 类对象
     * @param <T>   对象类型
     * @return 对象实例，创建失败返回null
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static <T> T newInstance(Class<T> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        try {
            return ReflectUtil.newInstance(clazz);
        } catch (Exception e) {
            log.error("创建实例失败: {}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 创建对象实例（使用指定参数的构造函数）
     *
     * @param clazz  类对象
     * @param params 构造函数参数
     * @param <T>    对象类型
     * @return 对象实例，创建失败返回null
     * @throws IllegalArgumentException 如果类对象为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz, Object... params) {
        validateNotNull(clazz, "类对象不能为null");
        try {
            return (T) ReflectUtil.newInstance(clazz, params);
        } catch (Exception e) {
            log.error("创建实例失败: {}, 参数: {}", clazz.getName(), Arrays.toString(params), e);
            return null;
        }
    }

    /**
     * 通过类名创建对象实例
     *
     * @param className 类的全限定名
     * @param <T>       对象类型
     * @return 对象实例，创建失败返回null
     * @throws IllegalArgumentException 如果类名为空
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) {
        validateNotBlank(className, "类名不能为空");
        Class<?> clazz = loadClass(className);
        if (clazz == null) {
            return null;
        }
        return (T) newInstance(clazz);
    }

    /**
     * 通过类名和参数创建对象实例
     *
     * @param className 类的全限定名
     * @param params    构造函数参数
     * @param <T>       对象类型
     * @return 对象实例，创建失败返回null
     * @throws IllegalArgumentException 如果类名为空
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Object... params) {
        validateNotBlank(className, "类名不能为空");
        Class<?> clazz = loadClass(className);
        if (clazz == null) {
            return null;
        }
        return (T) newInstance(clazz, params);
    }

    // ==================== 方法操作 ====================

    /**
     * 调用对象的方法
     *
     * @param obj        对象实例
     * @param methodName 方法名
     * @param args       方法参数
     * @param <T>        返回值类型
     * @return 方法返回值
     * @throws IllegalArgumentException 如果对象或方法名为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, String methodName, Object... args) {
        validateNotNull(obj, "对象实例不能为null");
        validateNotBlank(methodName, "方法名不能为空");

        try {
            return (T) ReflectUtil.invoke(obj, methodName, args);
        } catch (Exception e) {
            log.error("调用方法失败: {}.{}, 参数: {}",
                    obj.getClass().getName(), methodName, Arrays.toString(args), e);
            return null;
        }
    }

    /**
     * 调用静态方法
     *
     * @param clazz      类对象
     * @param methodName 方法名
     * @param args       方法参数
     * @param <T>        返回值类型
     * @return 方法返回值
     * @throws IllegalArgumentException 如果类对象或方法名为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeStatic(Class<?> clazz, String methodName, Object... args) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(methodName, "方法名不能为空");

        try {
            Method method = getMethod(clazz, methodName, getParameterTypes(args));
            if (method == null) {
                log.error("未找到静态方法: {}.{}", clazz.getName(), methodName);
                return null;
            }
            method.setAccessible(true);
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            log.error("调用静态方法失败: {}.{}, 参数: {}",
                    clazz.getName(), methodName, Arrays.toString(args), e);
            return null;
        }
    }

    /**
     * 获取方法对象
     *
     * @param clazz          类对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @return 方法对象，未找到返回null
     * @throws IllegalArgumentException 如果类对象或方法名为null
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(methodName, "方法名不能为空");

        String cacheKey = generateMethodCacheKey(clazz, methodName, parameterTypes);
        Method cachedMethod = METHOD_CACHE.get(cacheKey);
        if (cachedMethod != null) {
            return cachedMethod;
        }

        try {
            Method method = ReflectUtil.getMethod(clazz, methodName, parameterTypes);
            if (method != null) {
                cacheMethod(cacheKey, method);
            }
            return method;
        } catch (Exception e) {
            log.error("获取方法失败: {}.{}", clazz.getName(), methodName, e);
            return null;
        }
    }

    /**
     * 获取类的所有方法（包括私有方法）
     *
     * @param clazz 类对象
     * @return 所有方法列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(ReflectUtil.getMethods(clazz));
    }

    /**
     * 获取类的所有公共方法
     *
     * @param clazz 类对象
     * @return 所有公共方法列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Method> getPublicMethods(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(ReflectUtil.getPublicMethods(clazz));
    }

    /**
     * 获取指定名称的所有方法（支持方法重载）
     *
     * @param clazz      类对象
     * @param methodName 方法名
     * @return 匹配的方法列表
     * @throws IllegalArgumentException 如果类对象或方法名为null
     */
    public static List<Method> getMethodsByName(Class<?> clazz, String methodName) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(methodName, "方法名不能为空");

        return getAllMethods(clazz).stream()
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());
    }

    /**
     * 判断方法是否存在
     *
     * @param clazz          类对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @return 方法是否存在
     * @throws IllegalArgumentException 如果类对象或方法名为null
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return getMethod(clazz, methodName, parameterTypes) != null;
    }

    /**
     * 判断方法是否为静态方法
     *
     * @param method 方法对象
     * @return 是否为静态方法
     * @throws IllegalArgumentException 如果方法对象为null
     */
    public static boolean isStaticMethod(Method method) {
        validateNotNull(method, "方法对象不能为null");
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 判断方法是否为公共方法
     *
     * @param method 方法对象
     * @return 是否为公共方法
     * @throws IllegalArgumentException 如果方法对象为null
     */
    public static boolean isPublicMethod(Method method) {
        validateNotNull(method, "方法对象不能为null");
        return Modifier.isPublic(method.getModifiers());
    }

    // ==================== 字段操作 ====================

    /**
     * 获取字段值
     *
     * @param obj       对象实例
     * @param fieldName 字段名
     * @param <T>       字段类型
     * @return 字段值
     * @throws IllegalArgumentException 如果对象或字段名为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        validateNotNull(obj, "对象实例不能为null");
        validateNotBlank(fieldName, "字段名不能为空");

        try {
            return (T) ReflectUtil.getFieldValue(obj, fieldName);
        } catch (Exception e) {
            log.error("获取字段值失败: {}.{}", obj.getClass().getName(), fieldName, e);
            return null;
        }
    }

    /**
     * 获取静态字段值
     *
     * @param clazz     类对象
     * @param fieldName 字段名
     * @param <T>       字段类型
     * @return 字段值
     * @throws IllegalArgumentException 如果类对象或字段名为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(fieldName, "字段名不能为空");

        try {
            Field field = getField(clazz, fieldName);
            if (field == null) {
                log.error("未找到静态字段: {}.{}", clazz.getName(), fieldName);
                return null;
            }
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception e) {
            log.error("获取静态字段值失败: {}.{}", clazz.getName(), fieldName, e);
            return null;
        }
    }

    /**
     * 设置字段值
     *
     * @param obj       对象实例
     * @param fieldName 字段名
     * @param value     字段值
     * @return 是否设置成功
     * @throws IllegalArgumentException 如果对象或字段名为null
     */
    public static boolean setFieldValue(Object obj, String fieldName, Object value) {
        validateNotNull(obj, "对象实例不能为null");
        validateNotBlank(fieldName, "字段名不能为空");

        try {
            ReflectUtil.setFieldValue(obj, fieldName, value);
            return true;
        } catch (Exception e) {
            log.error("设置字段值失败: {}.{} = {}", obj.getClass().getName(), fieldName, value, e);
            return false;
        }
    }

    /**
     * 设置静态字段值
     *
     * @param clazz     类对象
     * @param fieldName 字段名
     * @param value     字段值
     * @return 是否设置成功
     * @throws IllegalArgumentException 如果类对象或字段名为null
     */
    public static boolean setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(fieldName, "字段名不能为空");

        try {
            Field field = getField(clazz, fieldName);
            if (field == null) {
                log.error("未找到静态字段: {}.{}", clazz.getName(), fieldName);
                return false;
            }
            field.setAccessible(true);
            field.set(null, value);
            return true;
        } catch (Exception e) {
            log.error("设置静态字段值失败: {}.{} = {}", clazz.getName(), fieldName, value, e);
            return false;
        }
    }

    /**
     * 获取字段对象
     *
     * @param clazz     类对象
     * @param fieldName 字段名
     * @return 字段对象，未找到返回null
     * @throws IllegalArgumentException 如果类对象或字段名为null
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotBlank(fieldName, "字段名不能为空");

        String cacheKey = clazz.getName() + "#" + fieldName;
        Field cachedField = FIELD_CACHE.get(cacheKey);
        if (cachedField != null) {
            return cachedField;
        }

        try {
            Field field = ReflectUtil.getField(clazz, fieldName);
            if (field != null) {
                cacheField(cacheKey, field);
            }
            return field;
        } catch (Exception e) {
            log.error("获取字段失败: {}.{}", clazz.getName(), fieldName, e);
            return null;
        }
    }

    /**
     * 获取类的所有字段（包括私有字段和父类字段）
     *
     * @param clazz 类对象
     * @return 所有字段列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(ReflectUtil.getFields(clazz));
    }

    /**
     * 获取类的所有公共字段
     *
     * @param clazz 类对象
     * @return 所有公共字段列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Field> getPublicFields(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(clazz.getFields());
    }

    /**
     * 判断字段是否存在
     *
     * @param clazz     类对象
     * @param fieldName 字段名
     * @return 字段是否存在
     * @throws IllegalArgumentException 如果类对象或字段名为null
     */
    public static boolean hasField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName) != null;
    }

    /**
     * 判断字段是否为静态字段
     *
     * @param field 字段对象
     * @return 是否为静态字段
     * @throws IllegalArgumentException 如果字段对象为null
     */
    public static boolean isStaticField(Field field) {
        validateNotNull(field, "字段对象不能为null");
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断字段是否为final字段
     *
     * @param field 字段对象
     * @return 是否为final字段
     * @throws IllegalArgumentException 如果字段对象为null
     */
    public static boolean isFinalField(Field field) {
        validateNotNull(field, "字段对象不能为null");
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 判断字段是否为公共字段
     *
     * @param field 字段对象
     * @return 是否为公共字段
     * @throws IllegalArgumentException 如果字段对象为null
     */
    public static boolean isPublicField(Field field) {
        validateNotNull(field, "字段对象不能为null");
        return Modifier.isPublic(field.getModifiers());
    }

    // ==================== 注解处理 ====================

    /**
     * 获取类上的注解
     *
     * @param clazz           类对象
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return 注解对象，不存在返回null
     * @throws IllegalArgumentException 如果类对象或注解类型为null
     */
    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return clazz.getAnnotation(annotationClass);
    }

    /**
     * 获取方法上的注解
     *
     * @param method          方法对象
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return 注解对象，不存在返回null
     * @throws IllegalArgumentException 如果方法对象或注解类型为null
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        validateNotNull(method, "方法对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return method.getAnnotation(annotationClass);
    }

    /**
     * 获取字段上的注解
     *
     * @param field           字段对象
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return 注解对象，不存在返回null
     * @throws IllegalArgumentException 如果字段对象或注解类型为null
     */
    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass) {
        validateNotNull(field, "字段对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return field.getAnnotation(annotationClass);
    }

    /**
     * 判断类是否有指定注解
     *
     * @param clazz           类对象
     * @param annotationClass 注解类型
     * @return 是否有注解
     * @throws IllegalArgumentException 如果类对象或注解类型为null
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return clazz.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断方法是否有指定注解
     *
     * @param method          方法对象
     * @param annotationClass 注解类型
     * @return 是否有注解
     * @throws IllegalArgumentException 如果方法对象或注解类型为null
     */
    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        validateNotNull(method, "方法对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return method.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断字段是否有指定注解
     *
     * @param field           字段对象
     * @param annotationClass 注解类型
     * @return 是否有注解
     * @throws IllegalArgumentException 如果字段对象或注解类型为null
     */
    public static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        validateNotNull(field, "字段对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * 获取类上的所有注解
     *
     * @param clazz 类对象
     * @return 所有注解列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Annotation> getAllAnnotations(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");
        return Arrays.asList(clazz.getAnnotations());
    }

    /**
     * 获取带有指定注解的所有字段
     *
     * @param clazz           类对象
     * @param annotationClass 注解类型
     * @return 带有指定注解的字段列表
     * @throws IllegalArgumentException 如果类对象或注解类型为null
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");

        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    /**
     * 获取带有指定注解的所有方法
     *
     * @param clazz           类对象
     * @param annotationClass 注解类型
     * @return 带有指定注解的方法列表
     * @throws IllegalArgumentException 如果类对象或注解类型为null
     */
    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        validateNotNull(clazz, "类对象不能为null");
        validateNotNull(annotationClass, "注解类型不能为null");

        return getAllMethods(clazz).stream()
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    // ==================== 泛型处理 ====================

    /**
     * 获取类的泛型参数类型
     *
     * @param clazz 类对象
     * @return 泛型参数类型列表
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static List<Type> getGenericTypes(Class<?> clazz) {
        validateNotNull(clazz, "类对象不能为null");

        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return Arrays.asList(parameterizedType.getActualTypeArguments());
        }
        return Collections.emptyList();
    }

    /**
     * 获取类的第一个泛型参数类型
     *
     * @param clazz 类对象
     * @return 第一个泛型参数类型，不存在返回null
     * @throws IllegalArgumentException 如果类对象为null
     */
    public static Type getFirstGenericType(Class<?> clazz) {
        List<Type> types = getGenericTypes(clazz);
        return types.isEmpty() ? null : types.get(0);
    }

    /**
     * 获取字段的泛型类型
     *
     * @param field 字段对象
     * @return 泛型类型列表
     * @throws IllegalArgumentException 如果字段对象为null
     */
    public static List<Type> getFieldGenericTypes(Field field) {
        validateNotNull(field, "字段对象不能为null");

        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            return Arrays.asList(parameterizedType.getActualTypeArguments());
        }
        return Collections.emptyList();
    }

    /**
     * 获取方法返回值的泛型类型
     *
     * @param method 方法对象
     * @return 泛型类型列表
     * @throws IllegalArgumentException 如果方法对象为null
     */
    public static List<Type> getMethodReturnGenericTypes(Method method) {
        validateNotNull(method, "方法对象不能为null");

        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            return Arrays.asList(parameterizedType.getActualTypeArguments());
        }
        return Collections.emptyList();
    }

    // ==================== 属性拷贝 ====================

    /**
     * 拷贝对象属性（浅拷贝）
     * <p>
     * 将源对象的属性值拷贝到目标对象，只拷贝同名且类型兼容的属性。
     * </p>
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否拷贝成功
     * @throws IllegalArgumentException 如果源对象或目标对象为null
     */
    public static boolean copyProperties(Object source, Object target) {
        validateNotNull(source, "源对象不能为null");
        validateNotNull(target, "目标对象不能为null");

        try {
            List<Field> sourceFields = getAllFields(source.getClass());
            List<Field> targetFields = getAllFields(target.getClass());

            Map<String, Field> targetFieldMap = targetFields.stream()
                    .collect(Collectors.toMap(Field::getName, field -> field, (f1, f2) -> f1));

            for (Field sourceField : sourceFields) {
                String fieldName = sourceField.getName();
                Field targetField = targetFieldMap.get(fieldName);

                if (targetField != null && !isFinalField(targetField)) {
                    Object value = getFieldValue(source, fieldName);
                    if (value != null) {
                        setFieldValue(target, fieldName, value);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("拷贝属性失败", e);
            return false;
        }
    }

    /**
     * 拷贝对象属性（忽略null值）
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否拷贝成功
     * @throws IllegalArgumentException 如果源对象或目标对象为null
     */
    public static boolean copyPropertiesIgnoreNull(Object source, Object target) {
        validateNotNull(source, "源对象不能为null");
        validateNotNull(target, "目标对象不能为null");

        try {
            List<Field> sourceFields = getAllFields(source.getClass());
            List<Field> targetFields = getAllFields(target.getClass());

            Map<String, Field> targetFieldMap = targetFields.stream()
                    .collect(Collectors.toMap(Field::getName, field -> field, (f1, f2) -> f1));

            for (Field sourceField : sourceFields) {
                String fieldName = sourceField.getName();
                Field targetField = targetFieldMap.get(fieldName);

                if (targetField != null && !isFinalField(targetField)) {
                    Object value = getFieldValue(source, fieldName);
                    if (value != null) {
                        setFieldValue(target, fieldName, value);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("拷贝属性失败", e);
            return false;
        }
    }

    // ==================== 缓存管理 ====================

    /**
     * 缓存类对象
     *
     * @param className 类名
     * @param clazz     类对象
     */
    private static synchronized void cacheClass(String className, Class<?> clazz) {
        if (CLASS_CACHE.size() >= MAX_CACHE_SIZE) {
            clearOldestCache(CLASS_CACHE, MAX_CACHE_SIZE / 2);
        }
        CLASS_CACHE.put(className, clazz);
    }

    /**
     * 缓存方法对象
     *
     * @param cacheKey 缓存键
     * @param method   方法对象
     */
    private static synchronized void cacheMethod(String cacheKey, Method method) {
        if (METHOD_CACHE.size() >= MAX_CACHE_SIZE) {
            clearOldestCache(METHOD_CACHE, MAX_CACHE_SIZE / 2);
        }
        METHOD_CACHE.put(cacheKey, method);
    }

    /**
     * 缓存字段对象
     *
     * @param cacheKey 缓存键
     * @param field    字段对象
     */
    private static synchronized void cacheField(String cacheKey, Field field) {
        if (FIELD_CACHE.size() >= MAX_CACHE_SIZE) {
            clearOldestCache(FIELD_CACHE, MAX_CACHE_SIZE / 2);
        }
        FIELD_CACHE.put(cacheKey, field);
    }

    /**
     * 清除最旧的缓存
     *
     * @param cache       缓存Map
     * @param removeCount 移除数量
     * @param <K>         键类型
     * @param <V>         值类型
     */
    private static <K, V> void clearOldestCache(Map<K, V> cache, int removeCount) {
        Iterator<K> iterator = cache.keySet().iterator();
        for (int i = 0; i < removeCount && iterator.hasNext(); i++) {
            iterator.next();
            iterator.remove();
        }
        log.debug("清除{}个最旧的缓存", removeCount);
    }

    /**
     * 清除所有缓存
     */
    public static synchronized void clearAllCaches() {
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        CLASS_CACHE.clear();
        log.info("所有反射缓存已清除");
    }

    /**
     * 获取缓存大小信息
     *
     * @return 缓存大小信息
     */
    public static Map<String, Integer> getCacheSizes() {
        Map<String, Integer> sizes = new HashMap<>(4);
        sizes.put("methodCache", METHOD_CACHE.size());
        sizes.put("fieldCache", FIELD_CACHE.size());
        sizes.put("classCache", CLASS_CACHE.size());
        return sizes;
    }

    // ==================== 工具方法 ====================

    /**
     * 生成方法缓存键
     *
     * @param clazz          类对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @return 缓存键
     */
    private static String generateMethodCacheKey(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        StringBuilder key = new StringBuilder(clazz.getName())
                .append("#")
                .append(methodName)
                .append("(");

        if (parameterTypes != null && parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) {
                    key.append(",");
                }
                key.append(parameterTypes[i].getName());
            }
        }
        key.append(")");
        return key.toString();
    }

    /**
     * 获取参数类型数组
     *
     * @param args 参数数组
     * @return 参数类型数组
     */
    private static Class<?>[] getParameterTypes(Object[] args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return parameterTypes;
    }

    /**
     * 验证对象不为null
     *
     * @param obj     对象
     * @param message 错误消息
     * @throws IllegalArgumentException 如果对象为null
     */
    private static void validateNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证字符串不为空
     *
     * @param str     字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为空
     */
    private static void validateNotBlank(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }
}
