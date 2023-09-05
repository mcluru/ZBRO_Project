package com.zbro.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.SellerUser;


public interface AdminSellerUserRepository extends JpaRepository<SellerUser, Long>{

	Page<SellerUser> findAll(Pageable pageable);
}
