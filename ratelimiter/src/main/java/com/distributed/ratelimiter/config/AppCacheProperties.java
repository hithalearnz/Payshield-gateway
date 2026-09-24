package com.distributed.ratelimiter.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = "app.cache")
public record AppCacheProperties(@NonNull Duration userByIdTtl, @NonNull Duration userByUsernameTtl) {
}
