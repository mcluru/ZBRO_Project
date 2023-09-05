package com.zbro.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zbro.admin.repository.AdminRoomRepository;
import com.zbro.admin.repository.AdminSellerUserRepository;
import com.zbro.model.Room;
import com.zbro.model.SellerUser;

@Service
public class AdminService {
	
	@Autowired
	private AdminRoomRepository roomRepo;
	
	@Autowired
	private AdminSellerUserRepository sellerRepo;
	
	
    public Page<SellerUser> findAll(Pageable pageable) {
        return sellerRepo.findAll(pageable);
    }

    public Page<SellerUser> searchByCriteria(String searchType, String searchKeyword, String memberType, String admission, Pageable pageable) {
    	// 필터 옵션을 저장할 리스트들을 생성
        List<String> types = new ArrayList<>();
        List<String> admis = new ArrayList<>();

        if ("사업자".equals(memberType)) {
            types.add("Biz");
        } else if ("개인".equals(memberType)) {
            types.add("Personal");
        }
            
        // 필터링된 SellerUser 엔티티들을 저장할 리스트를 생성
        List<SellerUser> filteredSellers = new ArrayList<>();
        Page<SellerUser> sellers = findAll(Pageable.unpaged());

        for (SellerUser seller : sellers.getContent()) {
            if (isMatched(seller, types, admission, searchType, searchKeyword)) {
                filteredSellers.add(seller);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredSellers.size());
        return new PageImpl<>(filteredSellers.subList(start, end), pageable, filteredSellers.size());
    }

    private boolean isMatched(SellerUser seller, List<String> types, String admission, String searchType, String searchKeyword) {
        if (types != null && !types.isEmpty() && !types.contains(seller.isBiz() ? "Biz" : "Personal")) {
            return false;
        }
        if (admission != null && !admission.isEmpty()) {
            if ("승인".equals(admission) && !seller.isAdmission()) {
                return false;
            } else if ("미승인".equals(admission) && seller.isAdmission()) {
                return false;
            }
        }
        if (searchType != null && searchKeyword != null) {
            if (searchType.equals("name") && !seller.getName().contains(searchKeyword)) {
                return false;
            } else if (searchType.equals("email") && !seller.getEmail().contains(searchKeyword)) {
                return false;
            } else if (searchType.equals("brokerName") && !seller.getBrokerName().contains(searchKeyword)) {
                return false;
            }
        }

        return true;
    }
	

    public void approveSeller(Long sellerId) {
        Optional<SellerUser> sellerOptional = sellerRepo.findById(sellerId);
        if (sellerOptional.isPresent()) {
            SellerUser seller = sellerOptional.get();
            if (!seller.isAdmission()) {
                seller.setAdmission(true);
                sellerRepo.save(seller);
            }
        }
    }	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public Page<Room> findAllOrderedByRoomId(Pageable pageable) {
        return roomRepo.findAll(pageable);
    }

    public Page<Room> searchRoomsBySellerOrBuildingNameOrAddress(String searchType, List<String> types, String searchKeyword, Pageable pageable) {
        List<Room> filteredRooms = new ArrayList<>();
        Page<Room> rooms = findAllOrderedByRoomId(pageable);

        for (Room room : rooms.getContent()) {
            if (isMatchedWithSearchCriteria(room, types, searchType, searchKeyword)) {
                filteredRooms.add(room);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredRooms.size());
        return new PageImpl<>(filteredRooms.subList(start, end), pageable, filteredRooms.size());
    }

    private boolean isMatchedWithSearchCriteria(Room room, List<String> types,String searchType, String searchKeyword) {
        if (types != null && !types.isEmpty() && !types.contains(room.getType().toString())) {
            return false;
        }       
    	if (searchType != null && searchKeyword != null) {
            if (searchType.equals("seller")) {
                return room.getSeller().getName().contains(searchKeyword);
            } else if (searchType.equals("broker")) {
                return room.getSeller().getBrokerName().contains(searchKeyword);
            } else if (searchType.equals("buildingName")) {
                return room.getBuildingName().contains(searchKeyword);
            } else if (searchType.equals("address")) {
                return room.getAddress().contains(searchKeyword);
            }
        }

        return true;
    }
}
