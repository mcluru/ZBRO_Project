package com.zbro.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zbro.model.Room;

public interface AdminRoomRepository extends JpaRepository<Room, Long>{

	Page<Room> findAll(Pageable pageable);
}
