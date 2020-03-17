package com.example.cloud.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author dengzhiming
 * @date 2020/3/13 22:56
 */
@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver(){
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getQueryParams().getFirst("username"))
        );
    }

    @Primary
    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName()
        );
    }
}
