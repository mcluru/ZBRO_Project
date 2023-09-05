package com.zbro.seller.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.zbro.dto.RoomReviewDTO;
import com.zbro.main.repository.RoomReviewRepository;
import com.zbro.model.Room;
import com.zbro.model.RoomOption;
import com.zbro.model.RoomOptionType;
import com.zbro.model.RoomPhoto;
import com.zbro.seller.repository.SellerRoomFavoriteRepository;
import com.zbro.model.RoomReview;
import com.zbro.model.SellerUser;
import com.zbro.seller.repository.SellerReviewRepository;
import com.zbro.seller.repository.SellerRoomOptionRepository;
import com.zbro.seller.repository.SellerRoomOptionTypeRepository;
import com.zbro.seller.repository.SellerRoomPhotoRepository;
import com.zbro.seller.repository.SellerRoomRepository;
import com.zbro.seller.repository.SellerRoomUserRepository;
import com.zbro.seller.repository.SellerRoomReviewRepository;

@Service
public class SellerRoomService {

	@Autowired
	private SellerRoomRepository roomRepo;
	
	@Autowired
	private SellerRoomOptionRepository roomOptionRepo;
	
	@Autowired
	private SellerRoomOptionTypeRepository roomOptionTypeRepo;
	
	@Autowired
	private SellerRoomPhotoRepository roomPhotoRepo;
	
	@Autowired
	private SellerRoomFavoriteRepository roomFavoriteRepo;
	
	@Autowired
	private SellerRoomUserRepository sellerUserRepo;
	
	@Autowired
	private SellerRoomReviewRepository roomReviewRepo;

	@Autowired
	private SellerReviewRepository sellerReviewRepo;
	
	
	
	@Value("${file.images.room-photo}")
	private String fileRoomPhotoPath;
	
	

	public void insertRoom(Room room) {
		roomRepo.save(room);
	}

	public void insertRoomOption(RoomOption roomOption) {
		roomOptionRepo.save(roomOption);
	}

	public List<RoomOptionType> getRoomOptionType() {
		List<RoomOptionType> findOptionTypes = roomOptionTypeRepo.findAll();

		Collections.sort(findOptionTypes, Comparator.comparingInt(RoomOptionType::getSortOrder));

		return findOptionTypes;	
	}

	public void insertRoomPhoto(RoomPhoto roomPhoto) {
		roomPhotoRepo.save(roomPhoto);
	}

	
	
	
	public Page<Room> getAllRoomsOrderedByRoomId(SellerUser seller, Pageable pageable) {
	    return roomRepo.findAllBySeller_SellerIdOrderByRoomId(seller.getSellerId(), pageable);
	}


	public Page<Room> searchRoomsByTypeOrBuildingNameOrAddress(SellerUser seller, List<String> types, String searchType, String searchKeyword, Pageable pageable) {
	    List<Room> filteredRooms = new ArrayList<>();
	    Page<Room> rooms = getAllRoomsOrderedByRoomId(seller, Pageable.unpaged());

	    for (Room room : rooms.getContent()) {
	        if (isMatched(room, types, searchType, searchKeyword)) {
	            filteredRooms.add(room);
	        }
	    }

	    int start = (int) pageable.getOffset();
	    int end = Math.min((start + pageable.getPageSize()), filteredRooms.size());
	    return new PageImpl<>(filteredRooms.subList(start, end), pageable, filteredRooms.size());
	}
	
    private boolean isMatched(Room room, List<String> types, String searchType, String searchKeyword) {
        if (types != null && !types.isEmpty() && !types.contains(room.getType().toString())) {
            return false;
        }

        if (searchType != null && searchKeyword != null) {
            if (searchType.equals("type")) {
                return room.getType().toString().contains(searchKeyword);
            } else if (searchType.equals("buildingName")) {
                return room.getBuildingName().contains(searchKeyword);
            } else if (searchType.equals("address")) {
                return room.getAddress().contains(searchKeyword);
            }
        }

        return true;
    }
    
    @Transactional
    public void deleteRooms(List<Long> roomIds) {
        for (Long roomId : roomIds) {
            Room room = roomRepo.findById(roomId).orElse(null);
            if (room != null) {
            	
            	roomReviewRepo.deleteByRoom(room); 
                roomPhotoRepo.deleteByRoom(room); 
                roomOptionRepo.deleteByRoom(room); 
                roomFavoriteRepo.deleteByRoom(room); 
                roomRepo.deleteById(roomId);
            }
        }
    }
    

	public List<Room> getRooms(Long sellerId) {
		Optional<SellerUser> sellerUser = sellerUserRepo.findById(sellerId);
		
		return roomRepo.findAllBySeller(sellerUser.get());
	}

	public Room getRoom(Long roomId) {
		return roomRepo.findById(roomId).get();
	}

	public List<RoomOption> getRoomOptions(Room findRoom) {
		return roomOptionRepo.findAllByRoom(findRoom);
	}

	public List<RoomPhoto> getRoomPhotos(Room findRoom) {
		return roomPhotoRepo.findAllByRoom(findRoom);
	}
	
	public List<String> getRoomPhotosName(Room room) {
		List<String> roomPhotoNames = new ArrayList<>();
		for(RoomPhoto roomPhoto : roomPhotoRepo.findAllByRoom(room)) {
			roomPhotoNames.add(roomPhoto.getFileName());
		}
		return roomPhotoNames;
	}

	public RoomPhoto getRoomPhoto(Long photoId) {
		return roomPhotoRepo.findById(photoId).get();
	}

	public Resource getImageResource(RoomPhoto roomPhoto) throws FileNotFoundException {
		
		File file = new File(fileRoomPhotoPath+roomPhoto.getFileName());
		
		if(file.exists() == false || file.isFile() == false) {
			throw new FileNotFoundException("file not found : " +fileRoomPhotoPath + roomPhoto.getUploadFile());
		}
		
		InputStream fis = new FileInputStream(file);
		Resource imageResource = new InputStreamResource(fis);
		return imageResource;
	}

	@Transactional
	public void delRoomOptions(Room room) {
		roomOptionRepo.deleteAllByRoom(room);	
	}

	@Transactional
	public void delRoomPhoto(String roomPhotoName) {
		roomPhotoRepo.deleteByFileName(roomPhotoName);
	}

	public void delRoom(Long roomId) {
		roomRepo.deleteById(roomId);
	}

		// 리뷰 상세 페이지 이동 메서드
	  public List<RoomReview> getRoomReviewDetail(Long reviewId) { 
		  List<RoomReview> sellerRoomReview2 = sellerReviewRepo.findByreviewId(reviewId); 
	  return sellerRoomReview2; 
	  
	  
	  }
	 
	
	// 리뷰(review)페이지 페이징처리 및 검색기능
	public Page<RoomReview> getRoomReviewSeller(RoomReviewDTO roomReviewDTO, Long sellerId, int page, int size, String searchType, String searchKeyword, List<String> types) {
	    // Sort는 정렬기준 Sort 객체 import 후, Pageable에 객체 대입
	    Sort sort = Sort.by("createDate").descending();
	    Pageable pageable = PageRequest.of(page, size, sort); // page는 1부터 시작하므로 1을 빼줌

	   return sellerReviewRepo.findByRoom_Seller_SellerId(sellerId, pageable);
	}

	public Page<RoomReview> searchRoomsByTypeOrBuildingNameOrAddressReview(RoomReviewDTO roomReviewDTO, Long sellerId, List<String> types, String searchType, String searchKeyword,  int page, int size) {
	    List<RoomReview> filteredReviews = new ArrayList<>();
	    Sort sort = Sort.by("createDate").descending();
	    Pageable pageable = PageRequest.of(page, size, sort);
	    Page<RoomReview> reviews = getRoomReviewSeller(roomReviewDTO, sellerId,page,size, searchType, searchKeyword,types);

	    for (RoomReview review : reviews.getContent()) {
	        if (isMatched(review, types, searchType, searchKeyword)) {
	            filteredReviews.add(review);
	        }
	    }

	    int start = (int) pageable.getOffset();
	    int end = Math.min((start + pageable.getPageSize()), filteredReviews.size());
	    return new PageImpl<>(filteredReviews.subList(start, end), pageable, filteredReviews.size());
	}

	private boolean isMatched(RoomReview review, List<String> types, String searchType, String searchKeyword) {
	    if (types != null && !types.isEmpty() && !types.contains(review.getRoom().getType().toString())) {
	        return false;
	    }

	    if (searchType != null && searchKeyword != null) {
	        if (searchType.equals("type")) {
	            return review.getRoom().getType().toString().contains(searchKeyword);
	        } else if (searchType.equals("buildingName")) {
	            return review.getRoom().getBuildingName().contains(searchKeyword);
	        } else if (searchType.equals("address")) {
	            return review.getRoom().getAddress().contains(searchKeyword);
	        }
	    }

	    return true;
	}
	// 리뷰페이지 페이징 및 검색 기능 끝
	
}
