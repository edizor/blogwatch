package com.baeldung.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;

@Configuration
@PropertySource("classpath:common.properties")
public class CommonConfig {

    @Bean
    public RateLimiter createRateLimiter() {
        return RateLimiter.create(1);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
