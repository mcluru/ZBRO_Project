package com.zbro.main.controller;

import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zbro.dto.RoomReviewDTO;
import com.zbro.dto.RoomSearchDTO;
import com.zbro.main.service.FavoriteService;
import com.zbro.main.service.RoomService;
import com.zbro.main.service.UserService;
import com.zbro.model.ConsumerUser;
import com.zbro.model.Favorite;
import com.zbro.model.Room;
import com.zbro.model.RoomPhoto;
import com.zbro.model.RoomReview;

import lombok.extern.slf4j.Slf4j;

import com.zbro.model.RoomOption;
import com.zbro.model.SellerUser;

@Slf4j
@Controller
@RequestMapping("/room")
public class RoomController {
	
	@Autowired
	private RoomService roomService;
	
	@Autowired
	private FavoriteService favService;

	@Autowired
	private UserService userService;
	
	
	
	
	@GetMapping("/search")
	public String searchView(RoomSearchDTO roomDTO, Authentication authentication, Model model) {
		
		List<RoomSearchDTO> roomDTOList = null;
		
		if(authentication != null) {
			List<String> authorityList = authentication.getAuthorities().stream().map(authority->authority.getAuthority()).collect(Collectors.toList());
			if(authorityList.contains("ROLE_CONSUMER") == true) {
				ConsumerUser consumerUser = userService.getConsumerUserByEmail(authentication.getName());
				roomDTOList = roomService.searchRoomAndFavorite(roomDTO, consumerUser);
			} else {
				roomDTOList = roomService.searchRoomAndFavorite(roomDTO, null);
			}
		} else {
			roomDTOList = roomService.searchRoomAndFavorite(roomDTO, null);
		}
		
		
		model.addAttribute("rooms", roomDTOList);
		model.addAttribute("roomSearchDTO", roomDTO);
		
		return "main/room/search";
	}
	// 상세페이지 모든 데이터 + 리뷰페이징처리
	@RequestMapping("/detail/{roomId}")
	public String detailView(@PathVariable("roomId")Long roomId, RoomReviewDTO ReviewDTO,Authentication authentication ,
							 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "2") int size,Model model) {
	    
		
		 // RoomReview 페이징 처리
        Page<RoomReview> roomReviews = roomService.getRoomReview(ReviewDTO,roomId, page, size);
	    Room room = roomService.findById(roomId);
	    SellerUser selleruser = new SellerUser();
	    selleruser.setSellerId(room.getSeller().getSellerId());
	    if(authentication != null) {
	    	//## authentication.getName() : 현재 로그인된 유저의 이메일
	    	log.info("#### authentication.getName() : {}", authentication.getName());
	    	//## authentication.getAuthorities() : 현재 로그인된 유저의 권한을 조회(구매자:ROLE_CONSUMER, 판매자:ROLE_SELLER)
	    	log.info("#### authentication.getAuthorities() : {}", authentication.getAuthorities().toArray()[0].toString());
	    	
	    	// Collection<GrantedAuthority> => List<String>
	    	// 권한을 list의 String형태로 변환(비교하기 쉽게)
	    	List<String> authorityList = authentication.getAuthorities().stream().map(authority->authority.getAuthority()).collect(Collectors.toList());
	    	
	    	//## 현재 로그인된 유저 이메일을 사용하여 유저 Entity를 조회
	    	if(authorityList.contains("ROLE_CONSUMER")) {
	    	log.info("#### userService.getConsumerUserByEmail(authentication.getName()) : {}", userService.getConsumerUserByEmail(authentication.getName()));
	    	ConsumerUser consumerUser = userService.getConsumerUserByEmail(authentication.getName());
	    	Optional<Favorite> roomDetailFavorite = favService.getFavoriteDetail(consumerUser, room);
	    	 model.addAttribute("isFavorite", roomDetailFavorite.isPresent());
	 	    
	 	    if (roomDetailFavorite.isPresent()) {
	 	        model.addAttribute("roomDetailFavorite", roomDetailFavorite.get());
	 	    }
	    
	    	} 

	    }
	    // 같은판매자의 다른 매물 필터링
	    List<Room> roomsame = roomService.findOtherRoomsBySellerId(selleruser, roomId);
		/* List<Room> roomsame = roomService.findBySellerId(selleruser); */
	    

	    List<RoomOption> roomOption = roomService.getroomOption(room);
	    List<RoomPhoto> roomPhotoList = roomService.getRoomPhtotList(roomId);
	    Page<RoomReview> roomReviewList = roomService.getRoomReview(ReviewDTO, roomId ,page, size);
	    System.out.println(roomPhotoList);
	    model.addAttribute("room", room);
	    model.addAttribute("roomsame", roomsame);
	    model.addAttribute("roomOptions", roomOption);
	    model.addAttribute("roomPhotoList", roomPhotoList);
	    model.addAttribute("roomReviews", roomReviewList);
	    model.addAttribute("roomReviewpage", roomReviews);
	   
			 
	    return "main/room/detail";
	}
	
	
	
	@PostMapping("/favorite")
	@ResponseBody
	public ResponseEntity<?> doFavorite(Room Room, Authentication authentication) {
		
		if(authentication != null) {
			List<String> authorityList = authentication.getAuthorities().stream().map(authority->authority.getAuthority()).collect(Collectors.toList());
			if(authorityList.contains("ROLE_CONSUMER") == false) {
				return ResponseEntity.internalServerError().body(null);
			}
		} else {
			return ResponseEntity.internalServerError().body(null);
		}
		
		log.info("doFavorite = {}", Room);
		Favorite fav = new Favorite();
		
		ConsumerUser consumerUser = userService.getConsumerUserByEmail(authentication.getName());
		fav.setUser(consumerUser);
		fav.setRoom(Room);
		
		Favorite savedFav = favService.favorite(fav);
		
		return ResponseEntity.ok().body(savedFav.getFavoriteId());
	}
	
	
	@PostMapping("/unfavorite")
	@ResponseBody
	public ResponseEntity<?> doUnfavorite(Favorite fav) {
		
		log.info("doUnfavorite = {}", fav);
		
		favService.unfavorite(fav);
		return ResponseEntity.ok().body("success");
	}
	
	
	
	@GetMapping("/photo")
	public ResponseEntity<Resource> getRoomPhotoImage(Room room) throws FileNotFoundException {
		
		RoomPhoto findedRoomPhoto = roomService.getRoomPhotoOne(room);
		
		Resource imageResource = roomService.getImageResource(findedRoomPhoto);
		
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageResource);
	}
	

	// 상세페이지 사진 업로드 구현
	@GetMapping("/photodetail")
	public ResponseEntity<Resource> getDetailRoomPhotoImg(RoomPhoto roomphoto) throws FileNotFoundException {
		
		RoomPhoto findedRoomPhoto = roomService.getRoomPhoto(roomphoto);
		
		Resource imageResource = roomService.getImageResource(findedRoomPhoto);
		
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageResource);
	}
	// 리뷰 전송
	/*
	 * @PostMapping("/review") public String postRoomReview(@RequestParam("roomId")
	 * Long roomId, RoomReview roomReview, Authentication authentication, Model
	 * model) {
	 * 
	 * if (authentication != null) { //## authentication.getName() : 현재 로그인된 유저의 이메일
	 * log.info("#### authentication.getName() : {}", authentication.getName());
	 * //## authentication.getAuthorities() : 현재 로그인된 유저의 권한을 조회(구매자:ROLE_CONSUMER,
	 * 판매자:ROLE_SELLER) log.info("#### authentication.getAuthorities() : {}",
	 * authentication.getAuthorities().toArray()[0].toString());
	 * 
	 * // Collection<GrantedAuthority> => List<String> // 권한을 list의 String형태로
	 * 변환(비교하기 쉽게) List<String> authorityList =
	 * authentication.getAuthorities().stream() .map(authority ->
	 * authority.getAuthority()) .collect(Collectors.toList());
	 * 
	 * //## 현재 로그인된 유저 이메일을 사용하여 유저 Entity를 조회 if
	 * (authorityList.contains("ROLE_CONSUMER")) { log.
	 * info("#### userService.getConsumerUserByEmail(authentication.getName()) : {}"
	 * , userService.getConsumerUserByEmail(authentication.getName())); ConsumerUser
	 * consumerUser = userService.getConsumerUserByEmail(authentication.getName());
	 * 
	 * // 중복 체크: 이미 해당 소비자가 해당 매물에 리뷰를 등록했는지 확인 boolean isDuplicateReview =
	 * !roomService.saveRoomReview(roomReview);
	 * model.addAttribute("isDuplicateReview", isDuplicateReview); } else if
	 * (authorityList.contains("ROLE_SELLER")) { SellerUser sellerUser =
	 * userService.getSellerUserByEmail(authentication.getName());
	 * model.addAttribute("isSeller", true); } }
	 * 
	 * Room room = new Room(); room.setRoomId(roomId); roomReview.setRoom(room);
	 * roomService.saveRoomReview(roomReview);
	 * 
	 * return "redirect:/room/detail/" + roomId; }
	 */
	// 리뷰 전송
		@PostMapping("/review")
		public String postRoomReview(@RequestParam("roomId") Long roomId, RoomReview roomReview,Authentication authentication, Model model) {
		   
		if(authentication != null) {
		//## authentication.getName() : 현재 로그인된 유저의 이메일
		log.info("#### authentication.getName() : {}", authentication.getName());
		//## authentication.getAuthorities() : 현재 로그인된 유저의 권한을 조회(구매자:ROLE_CONSUMER, 판매자:ROLE_SELLER)
		log.info("#### authentication.getAuthorities() : {}", authentication.getAuthorities().toArray()[0].toString());
		
		// Collection<GrantedAuthority> => List<String>
		// 권한을 list의 String형태로 변환(비교하기 쉽게)
		List<String> authorityList = authentication.getAuthorities().stream().map(authority->authority.getAuthority()).collect(Collectors.toList());
		
		//## 현재 로그인된 유저 이메일을 사용하여 유저 Entity를 조회
		if(authorityList.contains("ROLE_CONSUMER")) {
		log.info("#### userService.getConsumerUserByEmail(authentication.getName()) : {}", userService.getConsumerUserByEmail(authentication.getName()));
		ConsumerUser consumerUser = userService.getConsumerUserByEmail(authentication.getName());
		roomReview.setUser(consumerUser);
		} else if(authorityList.contains("ROLE_SELLER")) {
			SellerUser sellerUser = userService.getSellerUserByEmail(authentication.getName());
	    	model.addAttribute("errorMessage", "판매자는 후기를 등록할 수 없습니다.");

	    	return "redirect:room/detail/" + roomId;
	    	
		}

	}
		Room room = new Room();
		room.setRoomId(roomId);
		roomReview.setRoom(room);
		roomService.saveRoomReview(roomReview); 
		
		    
		    return "redirect:/room/detail/" + roomId;
		}
		
	
}	

