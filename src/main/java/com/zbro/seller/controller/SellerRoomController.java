package com.zbro.seller.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.zbro.main.service.UserService;
import com.zbro.model.Room;
import com.zbro.model.RoomOption;
import com.zbro.model.RoomOptionType;
import com.zbro.model.RoomPhoto;
import com.zbro.model.SellerUser;
import com.zbro.seller.service.SellerRoomService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SellerRoomController {
	
	@Autowired
	private SellerRoomService roomService;
	@Autowired
	private UserService sellerUserService;
	
	@Value("${file.images.room-photo}")
	public String uploadFolder;
	
		// 매물등록 페이지 들어가기.
		@GetMapping("/seller/room/add")
		public String roomAddView(Model model,
								  RoomOptionType roomOptionType,
								  Principal principal) {
			List<RoomOptionType> optionTypes = roomService.getRoomOptionType();
			model.addAttribute("optionTypes", optionTypes);
			SellerUser myLoginInfo = sellerUserService.getSellerUserByEmail(principal.getName());
			model.addAttribute("loginedSellerId", myLoginInfo.getSellerId());
			
			return "seller/room/add";
		}

		// 매물 등록하기
		@PostMapping("/seller/room/add")
		public String roomAdd(@RequestParam("isRoomIn") boolean isRoomIn, 
							  @RequestParam("isElevator") boolean isElevator,
							  @RequestParam(value = "optionType", required = false) List<String> optionTypes,
							  @RequestParam(value = "uploadFile", required = false) List<MultipartFile> files,
							  Room room,
							  Principal principal,
							  Model model) throws Exception, IOException {
			
			SellerUser myLoginInfo = sellerUserService.getSellerUserByEmail(principal.getName());
			if(room.getSeller() != myLoginInfo) {
				model.addAttribute("message", "잘못된 접근입니다");
				return "common/alert";
			}
			
			// room insert
			room.setRoomIn(isRoomIn);
			room.setElevator(isElevator);
			roomService.insertRoom(room);
			
			// RoomOption insert
			if(optionTypes != null) {
				for (String optionType : optionTypes) {
					RoomOption roomOption = new RoomOption();
					RoomOptionType roomOptionType = new RoomOptionType();
					roomOptionType.setOptionType(optionType);
					roomOption.setRoom(room);
					roomOption.setOptionType(roomOptionType);
					roomService.insertRoomOption(roomOption);
				}
			}
			
			// 업로드한 파일 확인
//			for (MultipartFile file : files) {
//			    System.out.println("업로드한 파일 확인 : " + file.getOriginalFilename());
//			}
			
			int imgCnt = 1;
			for(MultipartFile file : files) {
				if(!file.getOriginalFilename().isBlank()) {	//파일을 업로드했다면
					// 지정폴더에 파일을 실제 업로드 // ex)room1_랜덤문자.파일형식
					String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
			        String randomFileName = "room" + room.getRoomId() + "_" + UUID.randomUUID().toString() + "." + fileExtension;
					file.transferTo(new File(uploadFolder + randomFileName));
					
					// 테이블 데이터 set
					RoomPhoto roomPhoto = new RoomPhoto();
					roomPhoto.setFileName(randomFileName);
					roomPhoto.setRoom(room);
					roomPhoto.setImageSeq(imgCnt);
					
					// img insert
					roomService.insertRoomPhoto(roomPhoto);
					imgCnt++;
				}
			}
			
			return "redirect:/seller/room/list";
		}
		
		
		@GetMapping("/seller/room/detail")
		public String sellerRoomDetail(Model model,
									   @RequestParam Long roomId,
									   Principal principal) {
			
			SellerUser myLoginInfo = sellerUserService.getSellerUserByEmail(principal.getName());
			Room findRoom = roomService.getRoom(roomId);
			SellerUser findRoomUser = sellerUserService.getSellerUser(findRoom.getSeller().getSellerId());
			
			if(myLoginInfo != findRoomUser) {
				model.addAttribute("message", "잘못된 접근입니다");
				return "common/alert";
			}
			
			List<RoomOptionType> roomOptionType = roomService.getRoomOptionType();
			List<RoomOption> roomOptions = roomService.getRoomOptions(findRoom);
			List<RoomPhoto> roomPhotos = roomService.getRoomPhotos(findRoom);
			
			model.addAttribute("room", findRoom);
			model.addAttribute("optionTypes", roomOptionType);
			model.addAttribute("thisRoomOptions", roomOptions);
			model.addAttribute("roomPhotos", roomPhotos);
			model.addAttribute("loginedSellerId", myLoginInfo.getSellerId());
			 
			return "seller/room/detail";
		}
		
		
		@GetMapping("/seller/room/photo")
		public ResponseEntity<Resource> getSellerRoomPhoto(RoomPhoto roomPhoto) throws FileNotFoundException {
			RoomPhoto findedRoomPhoto = roomService.getRoomPhoto(roomPhoto.getPhotoId());
			
			Resource imageResource = roomService.getImageResource(findedRoomPhoto);
			return ResponseEntity.ok().body(imageResource);
		}
		
		
		@PostMapping("/seller/room/edit")
		public String roomEdit(@RequestParam("isRoomIn") boolean isRoomIn, 
				  @RequestParam("isElevator") boolean isElevator,
				  @RequestParam(value = "optionType", required = false) List<String> optionTypes,
				  @RequestParam(value = "uploadFile", required = false) List<MultipartFile> files,
				  Room room,
				  @RequestParam("isPhotoEdit") int isPhotoEdit,
				  @RequestParam(value = "fileName", required = false) List<String> registRoomPhoto,
				  Model model,
				  Principal principal) throws Exception, IOException {
			
			SellerUser myLoginInfo = sellerUserService.getSellerUserByEmail(principal.getName());
			if(room.getSeller() != myLoginInfo) {
				model.addAttribute("message", "잘못된 접근입니다");
				return "common/alert";
			}
			
			// room edit
			room.setRoomIn(isRoomIn);
			room.setElevator(isElevator);
			roomService.insertRoom(room);
//			System.out.println("room : " + room.toString());
			
			// roomOption edit
			if(optionTypes != null) {
				// 앞단에서 가져온 옵션들
				Set<String> optionTypesSet = new HashSet<>(optionTypes);
				
				// 기존optionTypes의 optionType만 String으로 불러오기
				List<RoomOption> beforeRoomOption = roomService.getRoomOptions(room);
				Set<String> beforeRoomOptionSet = beforeRoomOption.stream()
						.flatMap(roomOption -> Stream.of(roomOption.getOptionType().getOptionType()))
						.collect(Collectors.toSet());
				
				// 옵션 변경되면 RoomOption delete/insert
				if(!beforeRoomOptionSet.equals(optionTypesSet)) {
					roomService.delRoomOptions(room);
					for (String optionType : optionTypes) {
						RoomOption roomOption = new RoomOption();
						RoomOptionType roomOptionType = new RoomOptionType();
						roomOptionType.setOptionType(optionType);
						roomOption.setRoom(room);
						roomOption.setOptionType(roomOptionType);
						roomService.insertRoomOption(roomOption);
					}
				}
			} else	roomService.delRoomOptions(room);
			

			List<String> RoomPhotoNames = roomService.getRoomPhotosName(room);
			if(isPhotoEdit != 0) {	// 수정/등록/삭제 중 택1 시
				List<String> newPhotos = new ArrayList<>(); //새로 등록할 파일명 저장리스트
				
				for(String RoomPhotoName : RoomPhotoNames) {
					if(!registRoomPhoto.contains(RoomPhotoName)) {
//						System.out.println(RoomPhotoName+"얘는 지워야함");
						File getFile = new File(uploadFolder + RoomPhotoName);
						getFile.delete();
						roomService.delRoomPhoto(RoomPhotoName);
					}
				}
				
				for(String photo : registRoomPhoto) { //input에 있는 파일명들 순차검사
					if(!RoomPhotoNames.contains(photo) && !photo.isBlank()) {	//input o, DB x
//						System.out.println(photo+"파일이 없넹");
						newPhotos.add(photo);
					}
//					else if(RoomPhotoNames.contains(photo) && !photo.isBlank()) {//input o, DB o
//						System.out.println(photo+"파일이 원래 있음");
//					}
				}
				
				if(!newPhotos.isEmpty()) {
					for(MultipartFile file : files) {
						String originalFilename = file.getOriginalFilename();
						if(newPhotos.contains(originalFilename)) {
							String fileExtension = FilenameUtils.getExtension(originalFilename);
		                    String randomFileName = "room" + room.getRoomId() + "_" + UUID.randomUUID().toString() + "." + fileExtension;
		                    file.transferTo(new File(uploadFolder + randomFileName));
		                    
		                // 테이블 데이터 set
						RoomPhoto roomPhoto = new RoomPhoto();
						roomPhoto.setFileName(randomFileName);
						roomPhoto.setRoom(room);
						
						// img insert
						roomService.insertRoomPhoto(roomPhoto);
						}
					}
				}
				int imgCount = 1;
				List<RoomPhoto> roomPhotos = roomService.getRoomPhotos(room);
				for(RoomPhoto roomPhoto : roomPhotos) {
					roomPhoto.setImageSeq(imgCount);
					roomService.insertRoomPhoto(roomPhoto);
					imgCount++;
				}
				
			}
//			else System.out.println("파일수정안함");
			
			return "redirect:/seller/room/detail?roomId="+room.getRoomId();
		}
		
		
		
		@GetMapping("/seller/room/delete")
		public String roomDelete(@RequestParam("roomId") Room room,
								 Principal principal,
								 Model model) {
			SellerUser myLoginInfo = sellerUserService.getSellerUserByEmail(principal.getName());
			if(room.getSeller() != myLoginInfo) {
				model.addAttribute("message", "잘못된 접근입니다");
				return "common/alert";
			}
			
			if(!roomService.getRoomOptions(room).isEmpty()) {
				roomService.delRoomOptions(room);
			}
			
			if(!roomService.getRoomPhotos(room).isEmpty()) {
				List<String> RoomPhotoNames = roomService.getRoomPhotosName(room);
				for(String RoomPhotoName : RoomPhotoNames) {
					File getFile = new File(uploadFolder + RoomPhotoName);
					getFile.delete();
					roomService.delRoomPhoto(RoomPhotoName);
				}
			}
			
			roomService.delRoom(room.getRoomId());
			return "redirect:/seller/room/list";
		}
		
		
}

