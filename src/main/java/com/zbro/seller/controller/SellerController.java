package com.zbro.seller.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.zbro.dto.RoomReviewDTO;
import com.zbro.main.service.UserService;
import com.zbro.model.Room;
import com.zbro.model.RoomOption;
import com.zbro.model.RoomOptionType;
import com.zbro.model.RoomPhoto;
import com.zbro.model.RoomReview;
import com.zbro.model.SellerUser;
import com.zbro.seller.service.SellerRoomService;
import com.zbro.type.UserStatusType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SellerController {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private UserService sellerUserService;
	
	@Autowired
	private SellerRoomService roomService;
	
	
	@Value("${file.images.room-photo}")
	public String uploadFolder;
	
	@Value("${file.biz}")
	private String fileBizPath;
	
	
	
	
	@GetMapping("/seller")
	public String sellerMainPage() {
		
		return "redirect:/seller/user";
	}
	
	/**
	 * 판매자 로그인 성공했을때 오는 매핑
	 * 사이드바에 유저 정보를 항시 표출하기 위해 세션에 정보 추가
	 * */
	@GetMapping("/seller/login/success")
	public String sellerLoginSuccess(HttpSession session, Principal principal) {
		SellerUser seller = sellerUserService.getSellerUserByEmail(principal.getName());
		
		session.setAttribute("sellerId", seller.getSellerId());
		session.setAttribute("sellerName", seller.getName());
		session.setAttribute("sellerIsAdmission", seller.isAdmission());
		
		return "redirect:/seller";
	}
	

	@GetMapping("/seller/user")
	public String sellerUserView(Principal principal, Model model){
		
		
		SellerUser sellerUser = sellerUserService.getSellerUserByEmail(principal.getName());
		model.addAttribute("sellerUser", sellerUser);
		
		return "seller/user/detail";
	}
	
	@PostMapping("/seller/user/update")
	public String sellerUserUpdate(Principal principal
									, @RequestParam("profilePhotoFile") MultipartFile profilePhotoFile
									, @RequestParam("bizScanFile") MultipartFile bizScanFile
									, @RequestParam("isBiz") String isBizString
									, SellerUser sellerUser
									, HttpSession session
									) throws IllegalStateException, IOException {
		
		
		SellerUser findedSellerUser = sellerUserService.getSellerUserByEmail(principal.getName());
		
		//isBiz String -> boolean
		sellerUser.setBiz("true".equalsIgnoreCase(isBizString));
		
		log.info("#### sellerUserUpdate : {}", sellerUser);
		
		if(profilePhotoFile.isEmpty() == false) {
			sellerUserService.profileImageDelete(findedSellerUser);
			String profileImageFilename = sellerUserService.profileImageSave(profilePhotoFile);
			sellerUser.setProfilePhoto(profileImageFilename);
		}
		
		if(bizScanFile.isEmpty() == false && sellerUser.isBiz()) {
			sellerUserService.bizFileDelete(findedSellerUser);
			String bizFilename = sellerUserService.bizFileSave(bizScanFile);
			sellerUser.setBizFile(bizFilename);
		} else if(sellerUser.isBiz() == false) {
			sellerUserService.bizFileDelete(findedSellerUser);
		}
		
		SellerUser savedSeller = sellerUserService.updateSellerUser(findedSellerUser.getSellerId(), sellerUser);
		
		//사이드바 업데이트
		session.setAttribute("sellerName", savedSeller.getName());
		
		return "redirect:/seller/user";
	}
	
	@PostMapping("/seller/user/password")
	public ResponseEntity<?> sellerUserPasswordChange(@RequestParam("currentPassword") String currentPassword, @RequestParam("changePassword") String changePassword, Principal principal){
		
		log.info("currentPassword:{}, changePassword:{}",currentPassword ,changePassword);
		SellerUser sellerUser = sellerUserService.getSellerUserByEmail(principal.getName());
		String message = sellerUserService.sellerPasswordChange(sellerUser, currentPassword, changePassword);
		
		return ResponseEntity.ok().body(message);
	}
	
	@DeleteMapping("/seller/user/delete")
	@ResponseBody
	public ResponseEntity<?> sellerUserDelete(Principal principal) {
		SellerUser findedSellerUser = sellerUserService.getSellerUserByEmail(principal.getName());
		
		sellerUserService.deleteSellerUser(findedSellerUser);
		
		return ResponseEntity.ok().body(true);
	}
	
	
	
	
	
	@GetMapping("/download/seller/bizfile")
	public ResponseEntity<Resource> downloadBizFile(SellerUser user) {
		SellerUser sellerUser = sellerUserService.getSellerUser(user.getSellerId());
		
		try {
			Resource resource =  resourceLoader.getResource("file:"+fileBizPath + sellerUser.getBizFile());
			File file = resource.getFile();
			String filename = URLEncoder.encode(file.getName(), "UTF-8");
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename)
					.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM.toString())
					.body(resource);
			
		} catch (IOException e) {
			 return ResponseEntity.badRequest().body(null);
		}
		
	}


		
	
	
	

	@GetMapping("/seller/room/list")
	public String roomList(Model model,Principal principal,
	                       @RequestParam(value = "searchType", required = false) String searchType,
	                       @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
	                       @RequestParam(value = "type", required = false) List<String> types,
	                       @RequestParam(defaultValue = "0") int page,
	                       @RequestParam(defaultValue = "10") int size) {


		SellerUser sellerId = sellerUserService.getSellerUserByEmail(principal.getName());
	    Pageable pageable = PageRequest.of(page, size);

	    Page<Room> rooms;
	    if ((searchType != null && searchKeyword != null) || (types != null && !types.isEmpty())) {
	        rooms = roomService.searchRoomsByTypeOrBuildingNameOrAddress(sellerId, types, searchType, searchKeyword, pageable);
	    } else {
	        rooms = roomService.getAllRoomsOrderedByRoomId(sellerId, pageable);
	    }

	    model.addAttribute("rooms", rooms);
	    model.addAttribute("searchKeyword", searchKeyword);
	    model.addAttribute("searchType", searchType);
	    model.addAttribute("types", types);

	    return "seller/roomList";
	}
	
	
	@PostMapping("/seller/room/delete")
	public ResponseEntity<Void> deleteRooms(@RequestBody List<Long> roomIds) {
	    System.out.println(" roomIds: " + roomIds);

	    roomService.deleteRooms(roomIds);

	    return ResponseEntity.ok().build();
	}
	
	
	@GetMapping("seller/room/review")
	public Page<RoomReview> getroomReviewdetail(Principal principal,RoomReviewDTO ReviewDTO,@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10") int size, 
			@RequestParam(value = "searchType", required = false) String searchType, @RequestParam(value = "searchKeyword", required = false) String searchKeyword,@RequestParam(value = "type", required = false) 
			List<String> types,Model model) {
		// 판매자 아이디가져오기
		SellerUser sellerUser = sellerUserService.getSellerUserByEmail(principal.getName());
	    Long sellerId = sellerUser.getSellerId();
	    //아이디가져오기 끝    
	    //검색메서드 //오류 났던이유 페이지 불러온 메서드만 썻기 때문에, 검색기능에 들어간 reviews를 써야 호출이 되지
	    Page<RoomReview> reviews;
	    if ((searchType != null && searchKeyword != null) || (types != null && !types.isEmpty())) {
	        reviews = roomService.searchRoomsByTypeOrBuildingNameOrAddressReview(ReviewDTO, sellerId,types,searchType,searchKeyword, page, size );
	    } else {
	        reviews = roomService.getRoomReviewSeller(ReviewDTO, sellerId, page, size, searchKeyword, searchKeyword, types);
	    }
	    
	    model.addAttribute("reviews", reviews);
	    model.addAttribute("searchKeyword", searchKeyword);
	    model.addAttribute("searchType", searchType);
	    model.addAttribute("types", types);
	    return reviews;
	}
	
		// 리뷰 상세페이지 메서드
	  @GetMapping("seller/room/reviewdetail") 
	  public List<RoomReview> getroomReviewdetaiList(@RequestParam("reviewId")Long reviewId, Model model) {
	  List<RoomReview> roomReview3 = roomService.getRoomReviewDetail(reviewId);
	  System.out.println(roomReview3);
	  model.addAttribute("reviews", roomReview3);
	  return roomReview3; 
	  
	  
	  }
	 
	
}
	
