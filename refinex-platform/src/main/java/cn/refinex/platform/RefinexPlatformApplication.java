package cn.refinex.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex 平台应用主类
 *
 * @author Refinex
 * @since 1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"cn.refinex"})
public class RefinexPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexPlatformApplication.class, args);
    }
}
