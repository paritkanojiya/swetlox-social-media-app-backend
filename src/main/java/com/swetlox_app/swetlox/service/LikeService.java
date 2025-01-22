package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.Like;
import com.swetlox_app.swetlox.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;

    public Like like(Like like){
        return likeRepository.save(like);
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

    public LikeResponseDto entityToLikeResponseDto(Like like){
        UserDto sender = userService.getUserDtoById(like.getUserId());
        return LikeResponseDto.builder().id(like.getId())
                .sender(sender)
                .createdAt(like.getCreatedAt())
                .build();
    }
}
