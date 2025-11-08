package cn.refinex.satoken.reactor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 白名单配置类
 *
 * @author Refinex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "security.ignore")
public class SaTokenWhiteProperties {

    /**
     * 白名单路径
     */
    private List<String> whites = new ArrayList<>();
}
