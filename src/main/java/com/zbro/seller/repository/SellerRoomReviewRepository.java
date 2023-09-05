package com.zbro.seller.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.zbro.model.Room;
import com.zbro.model.RoomReview;

public interface SellerRoomReviewRepository extends JpaRepository<RoomReview, Long>  {
	
	
	void deleteByRoom(Room room);
}
