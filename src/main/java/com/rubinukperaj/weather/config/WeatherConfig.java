package com.rubinukperaj.weather.config;

import com.rubinukperaj.weather.cache.WeatherCache;
import com.rubinukperaj.weather.web.dto.WeatherResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WeatherConfig {

    private ExecutorService providerExecutor;

    @Bean
    public ExecutorService providerExecutor() {
        providerExecutor = Executors.newFixedThreadPool(6);
        return providerExecutor;
    }

    @Bean
    public WeatherCache<WeatherResponse> weatherCache(
            @Value("${weather.cache.ttl-minutes}") long ttlMinutes) {
        return new WeatherCache<>(Duration.ofMinutes(ttlMinutes));
    }

    @PreDestroy
    public void shutdown() {
        if (providerExecutor != null) {
            providerExecutor.shutdown();
        }
    }
}
