package com.distributed.ratelimiter.service;

import com.distributed.ratelimiter.dto.CachedUserSummary;
import com.distributed.ratelimiter.repository.MerchantRepository;

import java.util.UUID;
import java.util.Objects;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCacheService {

	private final MerchantRepository merchantRepository;

	public UserCacheService(MerchantRepository merchantRepository) {
		this.merchantRepository = merchantRepository;
	}

	@Cacheable(cacheNames = "userByUsername", key = "#username", unless = "#result == null")
	@Transactional(readOnly = true)
	public CachedUserSummary getByUsername(String username) {
		return merchantRepository.findByUsername(username)
				.map(CachedUserSummary::fromEntity)
				.orElse(null);
	}

	@Cacheable(cacheNames = "userById", key = "#id", unless = "#result == null")
	@Transactional(readOnly = true)
	public CachedUserSummary getById(UUID id) {
		return merchantRepository.findById(Objects.requireNonNull(id, "id must not be null"))
				.map(CachedUserSummary::fromEntity)
				.orElse(null);
	}
}