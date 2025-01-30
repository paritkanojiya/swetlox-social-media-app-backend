package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.Like;
import com.swetlox_app.swetlox.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    @Autowired
    @Lazy
    private  UserService userService;
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;

    public Like save(Like like){
        return likeRepository.save(like);
    }

    public void update(Like like){
        likeRepository.save(like);
    }

    public void deleteLikeByEntityId(String entityId){
        likeRepository.deleteByEntityId(entityId);
    }

    public Page<LikeResponseDto> getLikeByEntityId(String entityId, Integer pageNum){

        PageRequest pageRequest = PageRequest.of(pageNum, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.DESC, "createdAt");
        Page<Like> likePage = likeRepository.findByEntityId(entityId, pageRequest);
        List<LikeResponseDto> likeResponseDtoList = likePage.map(this::entityToLikeResponseDto).toList();
        return new PageImpl<>(likeResponseDtoList,pageRequest,likePage.getTotalPages());
    }
    
    public List<LikeResponseDto> getLikeListByEntityId(String entityId){
        List<Like> likeList = likeRepository.findByEntityId(entityId);
        return likeList.stream().map(this::entityToLikeResponseDto).toList();
    }

    public LikeResponseDto entityToLikeResponseDto(Like like){
        UserDto sender = userService.getUserDtoById(like.getUserId());
        return LikeResponseDto.builder().id(like.getId())
                .sender(sender)
                .createdAt(like.getCreatedAt())
                .build();
    }

    public void deleteByEntityId(String entityId){
        likeRepository.deleteByEntityId(entityId);
    }

    public boolean isLike(String entityId,String userId){
        return likeRepository.existsByEntityIdAndUserIdAndLiked(entityId,userId,true);
    }

    public long countLikeEntityByEntityIdAndEntityType(String entityId, EntityType entityType){
        return likeRepository.countByEntityIdAndEntityTypeAndLiked(entityId,entityType,true);
    }

    public boolean unlike(String entityId, String authId) {
        Optional<Like> optionalLike = likeRepository.findByEntityIdAndUserId(entityId, authId);
        if(optionalLike.isPresent()){
            Like like = optionalLike.get();
            like.setLiked(false);
            update(like);
            return true;
        }
        return false;
    }

    public Optional<Like> isExist(String storyId, String id) {
        return likeRepository.findByEntityIdAndUserId(storyId,id);
    }

    public void deleteAllLikeByUserId(String id) {
        likeRepository.deleteByUserId(id);
    }

    public Integer getLikeCountByUserId(String userId) {
        return likeRepository.countByUserId(userId);
    }
    
    
    
}
