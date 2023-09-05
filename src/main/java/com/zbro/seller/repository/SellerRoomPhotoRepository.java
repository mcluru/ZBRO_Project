package com.zbro.seller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.Room;
import com.zbro.model.RoomPhoto;

public interface SellerRoomPhotoRepository extends JpaRepository<RoomPhoto, Long>{
	
	 void deleteByRoom(Room room);

	List<RoomPhoto> findAllByRoom(Room findRoom);

	void deleteAllByRoom(Room room);

	void deleteByFileName(String roomPhotoName);

}
