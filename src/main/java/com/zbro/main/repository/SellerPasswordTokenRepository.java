package com.zbro.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.ConsumerPasswordToken;
import com.zbro.model.SellerPasswordToken;
import com.zbro.model.SellerUser;

public interface SellerPasswordTokenRepository extends JpaRepository<SellerPasswordToken, Long> {

	Optional<SellerPasswordToken> findByUser(SellerUser sellerUser);

	Optional<SellerPasswordToken> findByToken(String token);
	
}
