package com.zbro.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zbro.main.repository.CommentRepository;
import com.zbro.main.repository.CommunityRepository;
import com.zbro.main.repository.ConsumerUserRepository;
import com.zbro.main.repository.MypageRepository;
import com.zbro.main.repository.RoomOptionRepository;
import com.zbro.main.repository.RoomOptionTypeRepository;
import com.zbro.main.repository.RoomReviewRepository;
import com.zbro.model.Comment;
import com.zbro.model.Community;
import com.zbro.model.ConsumerUser;
import com.zbro.model.Favorite;
import com.zbro.model.Room;
import com.zbro.model.RoomOption;
import com.zbro.model.RoomOptionType;
import com.zbro.model.RoomReview;
import com.zbro.type.PostType;
import com.zbro.type.UserStatusType;

@Service
public class MypageService {
	
	@Autowired
	private MypageRepository mypageRepository;
	
	@Autowired
	private RoomOptionRepository roomOptionRepository;
	
	@Autowired
	private RoomOptionTypeRepository roomOptionTypeRepository;

	
	@Autowired
	private CommunityRepository communityRepository;
	
	@Autowired
	private ConsumerUserRepository consumerUserRepository;
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private RoomReviewRepository roomReviewRepository;

	
	private Long getLoggedInUserId() {
		// SecurityContextHolder : 사용자 정보와 인증 정보를 저장하며 접근하기 위해 사용하는 공간으로, 보안 과정에서 사용되는 정보의 저장소 역할
		// getContext : 사용자의 인증 정보와 관련된 security 연관 데이터의 집합을 저장
		// getAuthentication : Authentication 객체를 반환,로그인한 사용자의 인증 정보를 포함
		
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    if (authentication != null) {
	        String userEmail = authentication.getName();
	        ConsumerUser user = consumerUserRepository.findByEmail(userEmail)
	            .orElseThrow(() -> new UsernameNotFoundException("Consumer User Not Found"));
	        return user.getConsumerId();
	    }
	    
	    return null;
	}	
	
	
    public List<Favorite> getFavoritesByUserConsumerId() {
        Long userId = getLoggedInUserId();
        return mypageRepository.findByUserConsumerId(userId);
    }
      
    public List<RoomOptionType> getAllRoomOptionTypes() {
        return roomOptionTypeRepository.findAll();
    }
    
    public List<RoomOption> getAllRoomOptions() {
        return roomOptionRepository.findAll();
    }
    
   
    public void saveMemo(Long favoriteId, String memo) {
        Favorite favorite = mypageRepository.findById(favoriteId).orElse(null);
        if (favorite != null) {
            favorite.setMemo(memo);
            mypageRepository.save(favorite);
        }
    }
    
    
    
    public void deleteFavorite(Long favoriteId) {
        mypageRepository.deleteById(favoriteId);
    }
    
    
    public Page<Community> getCommunitiesAllTips(int page, int size) {
        Long userId = getLoggedInUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return communityRepository.findByUserConsumerIdAndTypeOrderByCreateDateDesc(userId, PostType.꿀팁, pageable);
    }

    public Page<Community> getCommunitiesAllQuestions(int page, int size) {
        Long userId = getLoggedInUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return communityRepository.findByUserConsumerIdAndTypeOrderByCreateDateDesc(userId, PostType.질문, pageable);
    }
	
	public Page<Comment> getCommentsByUser(int page, int size) {
	    Long userId = getLoggedInUserId();
	    Pageable pageable = PageRequest.of(page, size);
	    return commentRepository.findByUserConsumerId(userId, pageable);
	}
	
	
	public ConsumerUser getConsumerUser() {
	    Long userId = getLoggedInUserId();
	    return consumerUserRepository.findByConsumerId(userId);
	}
	
    public ConsumerUser updateUserInfo(ConsumerUser consumerUser) {       
        return consumerUserRepository.save(consumerUser);
    }
    
    public void memberDelete() {
        Long userId = getLoggedInUserId();
        ConsumerUser consumerUser = consumerUserRepository.findByConsumerId(userId);
        consumerUser.setStatus(UserStatusType.DELETE);
        consumerUserRepository.save(consumerUser);
    }
    
    public List<Community> getTop10() {
        Long userId = getLoggedInUserId();
        return communityRepository.findTop6ByUserConsumerIdOrderByCreateDateDesc(userId);
    }


    
	public List<Comment> getComments10ByUser() {
		Long userId = getLoggedInUserId();
		return commentRepository.findTop6ByUserConsumerIdOrderByCreateDateDesc(userId);
	}

	
	
	  public Page<RoomReview> getReviewsByLoggedInUser(int page, int size) { Long
	  userId = getLoggedInUserId(); Pageable pageable = PageRequest.of(page, size);
	  return roomReviewRepository.findByUserConsumerId(userId, pageable); }
	 
	 

    
}
