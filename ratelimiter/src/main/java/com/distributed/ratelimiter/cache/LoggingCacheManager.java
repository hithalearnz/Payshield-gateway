package com.distributed.ratelimiter.cache;

import java.util.Collection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class LoggingCacheManager implements CacheManager {

	private final CacheManager delegate;

	public LoggingCacheManager(CacheManager delegate) {
		this.delegate = delegate;
	}

	@Override
	@Nullable
	public Cache getCache(@NonNull String name) {
		Cache cache = delegate.getCache(name);
		if (cache == null) {
			return null;
		}
		return new LoggingCache(cache);
	}

	@Override
	@NonNull
	public Collection<String> getCacheNames() {
		return delegate.getCacheNames();
	}
}
