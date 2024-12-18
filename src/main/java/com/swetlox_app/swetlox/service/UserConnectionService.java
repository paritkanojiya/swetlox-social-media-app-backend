package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.controller.UserController;
import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.entity.UserConnection;
import com.swetlox_app.swetlox.event.SendConnectionRequestEvent;
import com.swetlox_app.swetlox.repository.UserConnectionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConnectionService {

    private final UserConnectionRepo userConnectionRepo;
    private final ApplicationEventPublisher eventPublisher;
    @Autowired
    private  UserService userService;

    @Async(value = "taskExecutor")
    public void following(String requestedUserId, User authUser){

        eventPublisher.publishEvent(new SendConnectionRequestEvent(this,authUser.getId(),authUser.getUserName(),requestedUserId));
    }

    @Transactional
    public void acceptRequest(User authUser,String requestedUserId){

        UserConnection authUserConnection = userConnectionRepo.findByUserId(authUser.getId()).orElseGet(() -> createNewConnection(authUser.getId()));
        UserConnection requestedUserConnection = userConnectionRepo.findByUserId(requestedUserId).orElseGet(() -> createNewConnection(requestedUserId));
        if(!authUserConnection.getFollowing().contains(requestedUserId)){
            authUserConnection.getFollowing().add(requestedUserId);
        }
        if(!requestedUserConnection.getFollower().contains(authUser.getId())){
            requestedUserConnection.getFollower().add(authUser.getId());
        }
        userConnectionRepo.saveAll(Arrays.asList(authUserConnection,requestedUserConnection));
    }
    
    private UserConnection createNewConnection(String authId) {
        UserConnection userConnection=new UserConnection();
        userConnection.setUserId(authId);
        userConnection.setFollowing(new ArrayList<>());
        userConnection.setFollower(new ArrayList<>());
        return userConnection;
    }
    
    public List<String> getFollowerList(String authId){
        UserConnection userConnection = userConnectionRepo.findByUserId(authId).get();
        return userConnection.getFollower();
    }


    public List<String> getFollowingList(String authId){
        UserConnection userConnection = userConnectionRepo.findByUserId(authId).get();
        return userConnection.getFollowing();
    }

    public boolean existsByIdAndFollowerContains(String authId,String userId){
        return userConnectionRepo.existsByIdAndFollowerContains(authId,userId);
    }
    public boolean existsByIdAndFollowingContains(String authId,String userId){
        return userConnectionRepo.existsByIdAndFollowingContains(authId,userId);
    }
    
    public List<UserDto> getFollowerAndFollowing(String authId){
        UserConnection userConnection = userConnectionRepo.findByUserId(authId).orElseThrow();
        List<UserDto> userDtoList=new ArrayList<>();
        Set<String> uniqueId=new HashSet<>();
        userConnection.getFollower().stream().filter(uniqueId::add).map(userService::getUserById).map(this::convertToUserDto).forEach( (userDtoList::add));
        userConnection.getFollowing().stream().filter(uniqueId::add).map(userService::getUserById).map(this::convertToUserDto).forEach( (userDtoList::add));
        return userDtoList;
    }

    private UserDto convertToUserDto(User user){
        return UserDto.builder()
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileURL(user.getProfileURL())
                .build();
    }

    public void deleteUserFromAllConnections(String id) {
        List<String> followingList = getFollowingList(id);
        List<String> followerList = getFollowerList(id);
        followerList.stream()
                .map(this::userConnection)
                .forEach(user -> {
                    if (user != null && user.getFollower() != null) {
                        boolean removed = user.getFollower().remove(id);
                        if (removed) {
                            userConnectionRepo.save(user);
                            System.out.println("Removed user " + id + " from follower of " + user.getUserId());
                        }
                    }
                });
        followingList.stream()
                .map(this::userConnection)
                .forEach(user -> {
                    if (user != null && user.getFollowing() != null) {
                        boolean removed = user.getFollowing().remove(id);
                        if (removed) {
                            userConnectionRepo.save(user);
                            System.out.println("Removed user " + id + " from following list of " + user.getUserId());
                        }
                    }
                });
        userConnectionRepo.deleteByUserId(id);
    }


    public UserConnection userConnection(String id){
        return userConnectionRepo.findByUserId(id).get();
    }
}
