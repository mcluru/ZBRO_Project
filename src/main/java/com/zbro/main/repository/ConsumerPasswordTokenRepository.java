package com.zbro.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.ConsumerPasswordToken;
import com.zbro.model.ConsumerUser;

public interface ConsumerPasswordTokenRepository extends JpaRepository<ConsumerPasswordToken, Long> {

	Optional<ConsumerPasswordToken> findByUser(ConsumerUser consumerUser);

	Optional<ConsumerPasswordToken> findByToken(String token);
	
}
