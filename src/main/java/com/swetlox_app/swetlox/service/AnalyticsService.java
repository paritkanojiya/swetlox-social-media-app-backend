package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.dto.analytics.AnalyticsResponseDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.entity.UserCollection;
import com.swetlox_app.swetlox.entity.UserConnection;
import com.swetlox_app.swetlox.repository.UserCollectionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {


    @Autowired
    @Lazy
    private UserService userService;
    @Autowired
    @Lazy
    private PostService postService;
    @Autowired
    @Lazy
    private ReelsService reelsService;
    @Autowired
    @Lazy
    private CommentService commentService;
    @Autowired
    @Lazy
    private LikeService likeService;
    @Autowired
    @Lazy
    private UserConnectionService userConnectionService;
    @Autowired
    @Lazy
    private UserCollectionRepo userCollectionRepo;
    
    
    public AnalyticsResponseDto getUserAnalyticsDetails(String token){
        User authUser = userService.getAuthUser(token);
        return getAnalyticsDto(authUser.getId());
    }
    
    private AnalyticsResponseDto getAnalyticsDto(String userId){
        Integer totalBookMark= userCollectionRepo.countByUserId(userId);
        Integer userPostCount = postService.getUserPostCount(userId);
        Integer totalReelCount = reelsService.getUserReelCount(userId);
        Integer totalComment = commentService.getCommentCountByUserId(userId);
        Integer totalLike = likeService.getLikeCountByUserId(userId);
        Integer totalFollowing = userConnectionService.getFollowingCount(userId);
        Integer totalFollower = userConnectionService.getFollowerCount(userId);

        return AnalyticsResponseDto.builder()
                .userId(userId)
                .totalBookMark(totalBookMark)
                .totalComment(totalComment)
                .totalLike(totalLike)
                .totalFollowing(totalFollowing)
                .totalFollower(totalFollower)
                .totalPost(userPostCount+totalReelCount)
                .build();
    }

}
