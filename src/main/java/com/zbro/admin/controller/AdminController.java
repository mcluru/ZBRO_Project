package com.zbro.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.zbro.admin.service.AdminService;
import com.zbro.model.Room;
import com.zbro.model.SellerUser;





@Controller
public class AdminController {

	
	
	@Autowired
	private AdminService adminService;
	
	

	@GetMapping("/admin/seller/list")
	public String sellerList(Model model,
				             @RequestParam(defaultValue = "0") int page,
				             @RequestParam(defaultValue = "10") int size,			
	                         @RequestParam(required = false) String searchType,
	                         @RequestParam(required = false) String searchKeyword,
	                         @RequestParam(required = false) String memberType,
	                         @RequestParam(required = false) String admission) {

		// 요청받은 페이지와 페이지당 항목 수 기반으로 페이지네이션 정보 생성
		Pageable pageable = PageRequest.of(page, size);
		// 판매자 목록을 검색 조건(searchType, searchKeyword, memberType, admission)에 따라 조회
	    Page<SellerUser> sellerListPage = adminService.searchByCriteria(searchType, searchKeyword, memberType, admission, pageable);

	    model.addAttribute("sellerList", sellerListPage);
	    model.addAttribute("searchType", searchType);
	    model.addAttribute("searchKeyword", searchKeyword);
	    model.addAttribute("memberType", memberType);
	    model.addAttribute("admission", admission);

	    return "admin/AdminSellerList";
	}

	@PostMapping("/admin/seller/approve")
	public String approveSeller(@RequestBody Map<String, Long> request) {
		 // 요청 본문에서 판매자 ID가져옴
	    Long sellerId = request.get("sellerId");
	    adminService.approveSeller(sellerId);
	    return "redirect:/admin/seller/list";
	}
	
	
	
	
	
	

	
	
	
	
	
	
    @GetMapping("/admin/room/list")
    public String roomList(Model model,
                           @RequestParam(value = "searchType", required = false) String searchType,
                           @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                           @RequestParam(value = "type", required = false) List<String> types,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> rooms;
        if ((searchType != null && searchKeyword != null) || (types != null && !types.isEmpty())) {
            rooms = adminService.searchRoomsBySellerOrBuildingNameOrAddress(searchType,types, searchKeyword, pageable);
        } else {
            rooms = adminService.findAllOrderedByRoomId(pageable);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("types", types);

        return "admin/AroomList";
    }

}
