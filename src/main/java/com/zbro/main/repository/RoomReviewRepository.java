package com.zbro.main.repository;



import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.zbro.dto.RoomReviewDTO;
import com.zbro.model.ConsumerUser;
import com.zbro.model.Room;
import com.zbro.model.RoomReview;

public interface RoomReviewRepository extends JpaRepository<RoomReview, Long>{

	/* List<RoomReview> findById(); */

	 List<RoomReview> findByRoomRoomId(Long roomId);


	Page<RoomReview> findByRoomRoomId(Long roomId, Pageable pageable);


	Page<RoomReview> findByUserConsumerId(Long userId, Pageable pageable);


	RoomReview findByUserAndRoom(ConsumerUser consumerUser, Room room);
	 

	
}
