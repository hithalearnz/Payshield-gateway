package com.distributed.ratelimiter.config;

import com.distributed.ratelimiter.cache.LoggingCacheManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;

@Configuration
@EnableCaching
public class RedisCacheConfig {

	@Bean
	public RedisCacheManager redisCacheManager(@NonNull RedisConnectionFactory connectionFactory,
			@NonNull AppCacheProperties props) {
		var jdk = new JdkSerializationRedisSerializer();
		RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jdk))
				.disableCachingNullValues()
				.computePrefixWith(cacheName -> "cache:" + cacheName + "::");

		Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
		perCache.put("userById", base.entryTtl(props.userByIdTtl()));
		perCache.put("userByUsername", base.entryTtl(props.userByUsernameTtl()));

		Duration defaultTtl = Objects.requireNonNull(Duration.ofMinutes(5));
		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(base.entryTtl(defaultTtl))
				.withInitialCacheConfigurations(perCache)
				.build();
	}

	@Bean
	@Primary
	public CacheManager cacheManager(RedisCacheManager redisCacheManager) {
		return new LoggingCacheManager(redisCacheManager);
	}
}
