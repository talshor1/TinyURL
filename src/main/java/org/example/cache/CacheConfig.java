package org.example.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager("tinyurl-by-code");
        mgr.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
        );
        return mgr;
    }
}
