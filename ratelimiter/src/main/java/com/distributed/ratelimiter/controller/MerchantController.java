package com.distributed.ratelimiter.controller;

import com.distributed.ratelimiter.dto.UserProfileResponse;
import com.distributed.ratelimiter.dto.UserUpdateRequest;
import com.distributed.ratelimiter.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchants")
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@GetMapping("/me")
	@Operation(summary = "Current merchant profile")
	public UserProfileResponse me(Authentication auth) {
		return merchantService.getProfileForCurrentUser(auth.getName());
	}

	@PutMapping("/me")
	@Operation(summary = "Update current merchant")
	public UserProfileResponse update(
			Authentication auth,
			@RequestBody UserUpdateRequest request) {

		return merchantService.updateCurrentUser(auth.getName(), request);
	}
}