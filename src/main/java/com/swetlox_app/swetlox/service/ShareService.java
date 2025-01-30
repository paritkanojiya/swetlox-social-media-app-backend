package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.dto.post.PostResponseDto;
import com.swetlox_app.swetlox.dto.share.ShareResponseDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.Like;
import com.swetlox_app.swetlox.entity.Post;
import com.swetlox_app.swetlox.entity.Reels;
import com.swetlox_app.swetlox.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final PostService postService;
    private final LikeService likeService;
    private final UserService userService;
    private final ReelsService reelsService;

    public ShareResponseDto getEntityByIdAndEntityType(String entityId, EntityType entityType,boolean isAuthenticated,String token){


        boolean isLiked = isAuthenticated && likeService.isLike(entityId, userService.getAuthUser(token).getId());
        boolean isBookMarked=isAuthenticated && postService.isBookMarked(entityId,userService.getAuthUser(token).getId());
       
        switch (entityType){
            case POST -> {
                Post post = postService.getPostById(entityId);
                UserDto userDto = userService.getUserDtoById(post.getUserId());
                return ShareResponseDto.builder()
                        .entityId(post.getId())
                        .entityType(EntityType.POST)
                        .entityURL(post.getPostURL())
                        .postUser(userDto)
                        .isAuthenticated(isAuthenticated)
                        .isLiked(isLiked).isBookMark(isBookMarked)
                        .createdAt(post.getCreatedAt())
                        .build();
            }
            case REEL -> {
                Reels reel = reelsService.getReelById(entityId);
                UserDto userDto = userService.getUserDtoById(reel.getUserId());
                return ShareResponseDto.builder()
                        .entityId(reel.getId())
                        .entityType(EntityType.REEL)
                        .entityURL(reel.getReelsURL())
                        .postUser(userDto)
                        .isAuthenticated(isAuthenticated)
                        .isLiked(isLiked).isBookMark(isBookMarked)
                        .createdAt(reel.getCreatedAt())
                        .build();
            }
            default ->
                throw new RuntimeException("other share is not supported");
        }
    }

}
