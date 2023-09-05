package com.zbro.dto;


import java.util.List;

import com.zbro.model.Room;

import lombok.Data;

@Data
public class IndexRoomListDTO {
	
	//고시원, 원룸/오피스텔
	private String viewType;
	
	private String regionFirstDepth;
	private String regionSecondDepth;
	private String regionThirdDepth;
	
	private String resultRegion;
	private List<Room> resultRoomList;
	
}
