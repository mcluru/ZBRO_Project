package com.zbro.main.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.zbro.dto.IndexRoomListDTO;
import com.zbro.dto.RoomReviewDTO;
import com.zbro.dto.RoomSearchDTO;
import com.zbro.main.repository.ConsumerUserRepository;
import com.zbro.main.repository.FavoritRepository;
import com.zbro.main.repository.RoomOptionRepository;
import com.zbro.main.repository.RoomPhotoRepository;
import com.zbro.main.repository.RoomRepository;
import com.zbro.main.repository.RoomReviewRepository;
import com.zbro.model.ConsumerUser;
import com.zbro.model.Favorite;
import com.zbro.model.Room;
import com.zbro.model.RoomOption;
import com.zbro.model.RoomPhoto;
import com.zbro.model.RoomReview;
import com.zbro.model.SellerUser;
import com.zbro.type.CostType;
import com.zbro.type.RoomType;



@Service
public class RoomService {
	
	@Value("${file.images.room-photo}")
	private String fileRoomPhotoPath; 
	

	@Autowired
	private RoomPhotoRepository RoomPhotoRepo;
	
	@Autowired
	private RoomRepository roomRepository;
	
	@Autowired
	private FavoritRepository favRepository;
	
	@Autowired
	private RoomOptionRepository roomOptionRepository;
	
	@Autowired
	private RoomReviewRepository roomReviewRepository;
	
	@Autowired
	private ConsumerUserRepository consumerUserRepository;
	
	
	
	public List<RoomSearchDTO> searchRoomAndFavorite(RoomSearchDTO roomDTO, ConsumerUser consumerUser) {
		
		List<Room> findedRooms = new ArrayList<Room>();
		
		if(roomDTO.getCostType() == CostType.none && roomDTO.getType() == RoomType.none) {
			findedRooms = roomRepository.findRoomsByAddressContaining(roomDTO.getSearchWord());
		} else if(roomDTO.getCostType() == CostType.none && roomDTO.getType() != RoomType.none) {
			findedRooms = roomRepository.findRoomsByTypeAndAddressContaining(roomDTO.getType(), roomDTO.getSearchWord());
		} else if(roomDTO.getType() == RoomType.none && roomDTO.getCostType() != CostType.none) {
			findedRooms = roomRepository.findRoomsByCostTypeAndAddressContaining(roomDTO.getCostType(), roomDTO.getSearchWord());
		} else {
			findedRooms = roomRepository.findRoomsByTypeAndCostTypeAndAddressContaining(roomDTO.getType(), roomDTO.getCostType(), roomDTO.getSearchWord());
		}
		
		List<RoomSearchDTO> roomDTOList = new ArrayList<>();
		for(Room room : findedRooms) {
			RoomSearchDTO findedRoomDTO = new RoomSearchDTO(room);
			if(consumerUser != null) {
				Optional<Favorite> findedFavorite = favRepository.findByUserAndRoom(consumerUser, room);
				if(findedFavorite.isPresent() == true) {
					findedRoomDTO.setFavorite(true);
					findedRoomDTO.setFavoriteId(findedFavorite.get().getFavoriteId());
				}
			}
			roomDTOList.add(findedRoomDTO);
		}
		
		return roomDTOList;
	}
	
	
	
	public Room findById(Long roomId) {
		Optional<Room> roomOptional =  roomRepository.findById(roomId);
		if (roomOptional.isPresent()) {
			return roomOptional.get();
	    }
		return null;
	}




	public List<RoomOption> getroomOption(Room room) {
		List<RoomOption> getRoomOption = roomOptionRepository.findByRoom(room);
		return getRoomOption;
	}
	
	public List<Room> findBySellerId(SellerUser selleruser) {
		List<Room> roomsame = roomRepository.findBySeller(selleruser);
		
		return roomsame;
	}
	
	
	

	public RoomPhoto getRoomPhotoOne(Room room) {
		
		Optional<RoomPhoto> finedRoomPhoto = RoomPhotoRepo.findByRoomAndImageSeq(room, 1);
		
		return finedRoomPhoto.get();
	}




	public Resource getImageResource(RoomPhoto findedRoomPhoto) throws FileNotFoundException {
		
		File file = new File(fileRoomPhotoPath + findedRoomPhoto.getFileName());
		
		if(file.exists() == false || file.isFile() == false) {
			throw new FileNotFoundException("file not found : " +fileRoomPhotoPath + findedRoomPhoto.getFileName());
		}

		InputStream fis = new FileInputStream(file);
		Resource imageResource = new InputStreamResource(fis);
		return imageResource;
	}


// 이미지 파일을 불러오는 메서드
	public List<RoomPhoto> getRoomPhtotList(Long roomId) {
		
		List<RoomPhoto> RoomPhotoList = RoomPhotoRepo.findByRoomRoomId(roomId);
		
		return RoomPhotoList;
	}


	public RoomPhoto getRoomPhoto(RoomPhoto roomPhoto) {
		
		RoomPhoto getRoomDetail = RoomPhotoRepo.findById(roomPhoto.getPhotoId()).get();
		
		return getRoomDetail;
	}
	
	public boolean saveRoomReview(RoomReview roomReview) {
	    ConsumerUser consumerUser = roomReview.getUser();
	    Room room = roomReview.getRoom();

	    // 중복 체크: 이미 해당 소비자가 해당 매물에 리뷰를 등록했는지 확인
	    RoomReview existingReview = roomReviewRepository.findByUserAndRoom(consumerUser, room);

	    if (existingReview != null) {
	        return false; // 이미 리뷰를 등록한 경우
	    }

	    roomReviewRepository.save(roomReview);
	    return true; // 리뷰 등록 성공
	}
	
	public  Page<RoomReview> getRoomReview(RoomReviewDTO roomReviewDTO,Long roomId , int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		Page<RoomReview> getRoomReview = roomReviewRepository.findByRoomRoomId(roomId, pageable);
		return getRoomReview;
		
	}

	
	//detail.html RoomReview 페이징
	/*
	 * public Page<RoomReview> getRoomReviewsByRoomId(Long roomId, int page, int
	 * size) { Pageable pageable = PageRequest.of(page, size); return
	 * roomReviewRepository.findByRoomRoomId(roomId, pageable); }
	 */
	// 같은 판매자의 다른 매물 중에서 겹치지 않는 매물 필터링 roomId를 비교해서 같지 않은 매물만 ArrayList<>에 집어 넣고 매물 표시
	public List<Room> findOtherRoomsBySellerId(SellerUser selleruser, Long RoomId) {
	    List<Room> allRoomsOfSeller = roomRepository.findBySeller(selleruser);
	    List<Room> otherRooms = new ArrayList<>();

	    for (Room room : allRoomsOfSeller) {
	        if (!room.getRoomId().equals(RoomId)) {
	            otherRooms.add(room);
	        }
	    }

	    return otherRooms;
	}

	public IndexRoomListDTO getRoomByRegion(List<RoomType> roomTypeList, IndexRoomListDTO indexRoomDTO) {
		
		IndexRoomListDTO resultIndexRoomDTO = new IndexRoomListDTO();
		
		List<Room> regionThirdRoomList = roomRepository.findTop4ByTypeInAndAddressContainingOrderByRoomIdDesc(roomTypeList, indexRoomDTO.getRegionThirdDepth());
		if(regionThirdRoomList.size() >= 4) {
			resultIndexRoomDTO.setResultRegion(indexRoomDTO.getRegionThirdDepth());
			resultIndexRoomDTO.setResultRoomList(regionThirdRoomList);
			return resultIndexRoomDTO;
		}
		
		List<Room> regionSecondRoomList = roomRepository.findTop4ByTypeInAndAddressContainingOrderByRoomIdDesc(roomTypeList, indexRoomDTO.getRegionSecondDepth());
		if(regionSecondRoomList.size() >= 4) {
			resultIndexRoomDTO.setResultRegion(indexRoomDTO.getRegionSecondDepth());
			resultIndexRoomDTO.setResultRoomList(regionSecondRoomList);
			return resultIndexRoomDTO;
		}
		
		List<Room> regionFirstRoomList = roomRepository.findTop4ByTypeInAndAddressContainingOrderByRoomIdDesc(roomTypeList, indexRoomDTO.getRegionFirstDepth());
		if(regionFirstRoomList.size() >= 4) {
			resultIndexRoomDTO.setResultRegion(indexRoomDTO.getRegionFirstDepth());
			resultIndexRoomDTO.setResultRoomList(regionFirstRoomList);
			return resultIndexRoomDTO;
		}
		
		List<Room> createDateOrderedRoomList = roomRepository.findTop4ByTypeInOrderByRoomIdDesc(roomTypeList);
		resultIndexRoomDTO.setResultRoomList(createDateOrderedRoomList);
		
		
		return resultIndexRoomDTO;
	}


}

