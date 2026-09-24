package com.distributed.ratelimiter.service;

import com.distributed.ratelimiter.dto.CachedUserSummary;
import com.distributed.ratelimiter.dto.UserProfileResponse;
import com.distributed.ratelimiter.dto.UserUpdateRequest;
import com.distributed.ratelimiter.entity.Merchant;
import com.distributed.ratelimiter.entity.Role;
import com.distributed.ratelimiter.repository.MerchantRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@SuppressWarnings("null")
public class MerchantService {

	private final MerchantRepository merchantRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserCacheService userCacheService;
	private final CacheManager cacheManager;

	public MerchantService(
			MerchantRepository merchantRepository,
			PasswordEncoder passwordEncoder,
			UserCacheService userCacheService,
			CacheManager cacheManager) {

		this.merchantRepository = merchantRepository;
		this.passwordEncoder = passwordEncoder;
		this.userCacheService = userCacheService;
		this.cacheManager = cacheManager;
	}

	@Transactional
	public UserProfileResponse register(String username, String email, String rawPassword) {

		if (merchantRepository.existsByUsername(username)) {
			throw new ResponseStatusException(CONFLICT, "username already exists");
		}

		if (merchantRepository.existsByEmail(email)) {
			throw new ResponseStatusException(CONFLICT, "email already exists");
		}

		Merchant merchant = new Merchant(
				username,
				email,
				passwordEncoder.encode(rawPassword),
				Role.USER,
				Instant.now());

		Merchant saved = merchantRepository.save(merchant);
		return toProfile(saved);
	}

	@Transactional(readOnly = true)
	public Optional<Merchant> findByUsernameWithPassword(String username) {
		return merchantRepository.findByUsername(username);
	}

	@Transactional(readOnly = true)
	public Optional<CachedUserSummary> findSummaryByUsername(String username) {
		return Optional.ofNullable(userCacheService.getByUsername(username));
	}

	@Transactional(readOnly = true)
	public Optional<CachedUserSummary> findSummaryById(UUID id) {
		return Optional.ofNullable(userCacheService.getById(id));
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfileByUsername(String username) {

		CachedUserSummary summary = userCacheService.getByUsername(username);

		if (summary == null) {
			throw new ResponseStatusException(NOT_FOUND);
		}

		return toProfile(summary);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfileById(UUID id) {

		CachedUserSummary summary = userCacheService.getById(id);

		if (summary == null) {
			throw new ResponseStatusException(NOT_FOUND);
		}

		return toProfile(summary);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfileForCurrentUser(String username) {
		return getProfileByUsername(username);
	}

	@Transactional
	public UserProfileResponse updateCurrentUser(
			String username,
			UserUpdateRequest request) {

		Merchant merchant = merchantRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

		evictUserCaches(merchant);

		if (request.email() != null && !request.email().isBlank()) {

			if (merchantRepository.existsByEmail(request.email())
					&& !request.email().equalsIgnoreCase(merchant.getEmail())) {

				throw new ResponseStatusException(CONFLICT, "email already exists");
			}

			merchant.setEmail(request.email());
		}

		if (request.newPassword() != null && !request.newPassword().isBlank()) {

			if (request.currentPassword() == null
					|| request.currentPassword().isBlank()) {

				throw new ResponseStatusException(
						FORBIDDEN,
						"current password required");
			}

			if (!passwordEncoder.matches(
					request.currentPassword(),
					merchant.getPassword())) {

				throw new ResponseStatusException(
						FORBIDDEN,
						"invalid current password");
			}

			merchant.setPassword(
					passwordEncoder.encode(request.newPassword()));
		}

		Merchant saved = merchantRepository.save(merchant);
		return toProfile(saved);
	}

	private void evictUserCaches(Merchant merchant) {

		var byId = cacheManager.getCache("userById");
		if (byId != null) {
			byId.evict(merchant.getId());
		}

		var byUsername = cacheManager.getCache("userByUsername");
		if (byUsername != null) {
			byUsername.evict(merchant.getUsername());
		}
	}

	private UserProfileResponse toProfile(CachedUserSummary summary) {
		return new UserProfileResponse(
				summary.id(),
				summary.username(),
				summary.email(),
				summary.role().name(),
				summary.createdAt());
	}

	private UserProfileResponse toProfile(Merchant merchant) {
		return new UserProfileResponse(
				merchant.getId(),
				merchant.getUsername(),
				merchant.getEmail(),
				merchant.getRole().name(),
				merchant.getCreatedAt());
	}
}