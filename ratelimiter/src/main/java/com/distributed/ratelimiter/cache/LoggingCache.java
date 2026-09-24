package com.distributed.ratelimiter.cache;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class LoggingCache implements Cache {

	private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);

	private final Cache delegate;

	public LoggingCache(Cache delegate) {
		this.delegate = delegate;
	}

	@Override
	@NonNull
	public String getName() {
		return delegate.getName();
	}

	@Override
	@NonNull
	public Object getNativeCache() {
		return delegate.getNativeCache();
	}

	@Override
	@Nullable
	public ValueWrapper get(@NonNull Object key) {
		ValueWrapper w = delegate.get(key);
		log.info("cache_event type=get name={} key={} result={}", getName(), key, w != null ? "HIT" : "MISS");
		return w;
	}

	@Override
	@Nullable
	public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
		T value = delegate.get(key, type);
		log.info("cache_event type=getTyped name={} key={} result={}", getName(), key, value != null ? "HIT" : "MISS");
		return value;
	}

	@Override
	public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
		return delegate.get(key, () -> {
			log.info("cache_event type=getLoad name={} key={} result=MISS_COMPUTE", getName(), key);
			return valueLoader.call();
		});
	}

	@Override
	public void put(@NonNull Object key, @Nullable Object value) {
		delegate.put(key, value);
		log.debug("cache_event type=put name={} key={}", getName(), key);
	}

	@Override
	public void evict(@NonNull Object key) {
		delegate.evict(key);
		log.info("cache_event type=evict name={} key={}", getName(), key);
	}

	@Override
	public boolean evictIfPresent(@NonNull Object key) {
		boolean removed = delegate.evictIfPresent(key);
		if (removed) {
			log.info("cache_event type=evictIfPresent name={} key={}", getName(), key);
		}
		return removed;
	}

	@Override
	public void clear() {
		delegate.clear();
		log.info("cache_event type=clear name={}", getName());
	}
}
