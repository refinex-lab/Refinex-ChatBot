package cn.refinex.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex AI 应用主类
 *
 * @author Refinex
 * @since 1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"cn.refinex"})
public class RefinexAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexAiApplication.class, args);
    }
}
