package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.dto.admin.StatusDto;
import com.swetlox_app.swetlox.dto.admin.UserDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.util.StompEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {


    private final UserService userService;
    private final PostService postService;
    private final UserConnectionService userConnectionService;
    private final ReelsService reelsService;
    private final StompEventListener stompEventListener;


    public StatusDto getStatus(){
        return StatusDto.builder()
                .activeUser(getActiveUser())
                .pastWeekUser(getTotalPastWeekUser())
                .reportedPost(getTotalReportedPost())
                .totalPost(getTotalPost())
                .totalUser(getTotalUser())
                .build();
    }

    public Page<UserDto> getUserData(Integer pageNum){
        Page<User> allUser = userService.getAllUser(pageNum);
        List<UserDto> userDtoList = allUser.map(this::entityToUserDto).toList();
        return new PageImpl<>(userDtoList,allUser.getPageable(),allUser.getTotalElements());
    }
    
    private UserDto entityToUserDto(User user){
        int userPostCount = postService.getUserPostCount(user.getId());
        int userReelCount = reelsService.getUserReelCount(user.getId());
        int followerCount = userConnectionService.getFollowerCount(user.getId());
        int followingCount = userConnectionService.getFollowingCount(user.getId());
        return UserDto.builder().userId(user.getId())
                .totalPost(userPostCount+userReelCount)
                .email(user.getEmail())
                .userName(user.getUserName())
                .email(user.getEmail())
                .totalFollower(followerCount)
                .totalFollowing(followingCount)
                .profileURL(user.getProfileURL())
                .suspense(user.isSuspense())
                .build();
    }

    private int getActiveUser(){
        return stompEventListener.getActiveUserCount();
    }

    private int getTotalUser(){
        return (int) userService.getNumOfUser();
    }

    private int getTotalPost(){
        int totalReelCount = reelsService.getTotalReelCount();
        long totalPostCount = postService.getTotalPostCount();
        return (int) totalPostCount+totalReelCount;
    }

    private int getTotalPastWeekUser(){
        return userService.getPastWeekNewUserCount();
    }

    private int getTotalReportedPost(){
            return 11;
    }


    public void deleteUser(String userId) {
        userService.deleteAccountPermanentByUserId(userId);
    }

    public void suspenseAccount(String userId){
        User user = userService.getUserById(userId);
        user.setSuspense(true);
        userService.updateUser(user);
    }

    public void unsuspenseAccount(String userId) {
        User user = userService.getUserById(userId);
        user.setSuspense(false);
        userService.updateUser(user);
    }
}
