package cn.refinex.json.util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * JSON 工具类（基于 Jackson 3 / Spring Boot 4）, 使用 JDK 21 record 简化不可变结构。
 *
 * @author Refinex
 * @since 1.0.0
 */
public record JsonUtils(ObjectMapper mapper) {

    /**
     * 构造器：注入 Spring 提供的 ObjectMapper/JsonMapper
     */
    public JsonUtils {
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");
    }

    /**
     * 将对象序列化为 JSON 字符串（紧凑格式）
     *
     * @param obj 要序列化的对象
     * @return 序列化后的 JSON 字符串
     */
    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串（带缩进格式）
     *
     * @param obj 要序列化的对象
     * @return 序列化后的 JSON 字符串（带缩进）
     */
    public String toJsonPretty(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("JSON pretty serialization failed", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     *
     * @param json 要反序列化的 JSON 字符串
     * @param type 目标对象的类型
     * @return 反序列化后的对象
     */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("JSON deserialization failed: " + type.getName(), e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象（使用 TypeReference）
     *
     * @param json 要反序列化的 JSON 字符串
     * @param ref  类型引用，用于指定目标对象的类型
     * @return 反序列化后的对象
     */
    public <T> T fromJson(String json, TypeReference<T> ref) {
        try {
            return mapper.readValue(json, ref);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("JSON deserialization failed (TypeReference)", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象（使用 JavaType）
     *
     * @param json     要反序列化的 JSON 字符串
     * @param javaType 目标对象的 JavaType 类型
     * @return 反序列化后的对象
     */
    public <T> T fromJson(String json, JavaType javaType) {
        try {
            @SuppressWarnings("unchecked")
            T value = (T) mapper.readValue(json, javaType);
            return value;
        } catch (JacksonException e) {
            throw new JsonRuntimeException("JSON deserialization failed (JavaType)", e);
        }
    }

    /**
     * 构造 JavaType 类型，用于反序列化参数化类型（如 List<String>）
     *
     * @param raw            原始类型（如 List.class）
     * @param parameterTypes 参数类型（如 String.class）
     * @return 构造的 JavaType 类型
     */
    public JavaType constructType(Class<?> raw, Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return mapper.getTypeFactory().constructType(raw);
        }
        JavaType[] paramTypes = new JavaType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = mapper.getTypeFactory().constructType(parameterTypes[i]);
        }
        return mapper.getTypeFactory().constructParametricType(raw, paramTypes);
    }

    /**
     * 自定义运行时异常，用于封装 JSON 相关的运行时异常
     */
    public static class JsonRuntimeException extends RuntimeException {
        public JsonRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
