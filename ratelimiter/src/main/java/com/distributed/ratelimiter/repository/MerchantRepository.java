package com.distributed.ratelimiter.repository;

import com.distributed.ratelimiter.entity.Merchant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

	Optional<Merchant> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}