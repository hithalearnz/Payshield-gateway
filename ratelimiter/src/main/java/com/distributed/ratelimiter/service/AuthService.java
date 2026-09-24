package com.distributed.ratelimiter.service;

import com.distributed.ratelimiter.dto.AuthResponse;
import com.distributed.ratelimiter.dto.LoginRequest;
import com.distributed.ratelimiter.dto.UserProfileResponse;
import com.distributed.ratelimiter.entity.Merchant;
import com.distributed.ratelimiter.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

	private final MerchantService merchantService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			MerchantService merchantService,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {

		this.merchantService = merchantService;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public UserProfileResponse register(
			String username,
			String email,
			String rawPassword) {

		return merchantService.register(username, email, rawPassword);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {

		Merchant merchant = merchantService
				.findByUsernameWithPassword(request.username())
				.orElseThrow(() -> new ResponseStatusException(
						UNAUTHORIZED,
						"invalid credentials"));

		if (!passwordEncoder.matches(
				request.password(),
				merchant.getPassword())) {

			throw new ResponseStatusException(
					UNAUTHORIZED,
					"invalid credentials");
		}

		String token = jwtService.generateToken(merchant);

		return new AuthResponse(
				token,
				merchant.getId(),
				merchant.getUsername(),
				merchant.getRole().name());
	}
}