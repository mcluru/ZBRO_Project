package com.zbro.seller.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.Room;
import com.zbro.model.RoomReview;
import com.zbro.model.SellerUser;


public interface SellerReviewRepository extends JpaRepository<RoomReview,Long>{

	
	
	 List<RoomReview> findByreviewId(Long reviewId); 
	 Page<RoomReview> findByRoom_Seller_SellerId(Long sellerId, Pageable pageable);
	 
	
	 
	 
	
}
