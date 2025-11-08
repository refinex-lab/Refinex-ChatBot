package cn.refinex.core.factory;

import cn.refinex.core.util.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Objects;

/**
 * YAML 属性源工厂类
 * <p>
 * 用于加载 YAML 格式的配置文件, 并将其转换为 Spring 的属性源(PropertySource)。
 * 该工厂类主要用于在 Spring Boot 应用中加载自定义的 YAML 配置文件, 提供更灵活的配置方式。
 *
 * @author Lion Li
 * @author Refinex
 * @since 1.0.0
 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    /**
     * 创建属性源
     * <p>
     * 该方法用于根据给定的资源创建属性源。如果资源是 YAML 格式的配置文件, 则使用 {@link YamlPropertiesFactoryBean} 解析其内容,
     * 并将其转换为 {@link PropertiesPropertySource} 返回。否则, 则调用父类的方法创建默认属性源。
     *
     * @param name     属性源名称, 可以为 {@code null}
     * @param resource 编码资源, 包含要加载的配置文件
     * @return 创建的属性源, 类型为 {@link PropertySource}
     * @throws IOException 如果加载配置文件时发生 I/O 错误
     */
    @NonNull
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        String sourceName = resource.getResource().getFilename();
        if (StringUtils.isNotBlank(sourceName) && Strings.CI.endsWithAny(sourceName, ".yml", ".yaml")) {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return new PropertiesPropertySource(sourceName, Objects.requireNonNull(factory.getObject()));
        }
        return super.createPropertySource(name, resource);
    }
}
