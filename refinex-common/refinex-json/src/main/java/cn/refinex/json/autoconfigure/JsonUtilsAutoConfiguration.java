package cn.refinex.json.autoconfigure;

import cn.refinex.json.util.JsonUtils;
import cn.refinex.json.util.JsonUtilsHolder;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

/**
 * 将 Spring Boot 自动配置的 ObjectMapper 注入并构建 JsonUtils
 * <p>
 * 在 Spring Boot 4 中能直接拿到 auto-configured JsonMapper（JsonMapper extends ObjectMapper）
 *
 * @author Refinex
 * @since 1.0.0
 */
@AutoConfiguration
public class JsonUtilsAutoConfiguration {

    /**
     * 自动配置的 ObjectMapper
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入自动配置的 ObjectMapper
     */
    public JsonUtilsAutoConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 构建 JsonUtils bean，将自动配置的 ObjectMapper 注入
     */
    @Bean
    @Primary
    public JsonUtils jsonUtils() {
        return new JsonUtils(objectMapper);
    }

    /**
     * 可选：把实例放到一个静态 holder，方便在非 Spring 管理的类中使用（谨慎使用）。
     * 推荐：优先在需要的类中注入 JsonUtils bean。
     */
    @PostConstruct
    public void initStaticHolder() {
        JsonUtilsHolder.set(JsonUtilsHolder.INSTANCE_KEY, jsonUtils());
    }
}
