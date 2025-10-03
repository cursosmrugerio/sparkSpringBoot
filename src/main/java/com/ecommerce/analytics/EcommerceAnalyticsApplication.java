package com.ecommerce.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcommerceAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceAnalyticsApplication.class, args);
    }
}
