package cn.refinex.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex 网关应用主类
 *
 * @author Refinex
 * @since 1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"cn.refinex"})
public class RefinexGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexGatewayApplication.class, args);
    }
}
