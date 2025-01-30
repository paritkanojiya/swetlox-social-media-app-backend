package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.ConnectionRequestStatus;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.notification.ConnectionRequestNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.UserConnectionDTO;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.ConnectionRequest;
import com.swetlox_app.swetlox.entity.RecentChatHistory;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.entity.UserConnection;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import com.swetlox_app.swetlox.repository.ConnectionRequestRepo;
import com.swetlox_app.swetlox.repository.UserConnectionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConnectionService {

    private final UserConnectionRepo userConnectionRepo;
    private final ConnectionRequestRepo connectionRequestRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatRoomService chatRoomService;
    private final UserPreferenceService userPreferenceService;
    @Autowired
    private  UserService userService;
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;


    public void followRequest(String requestedUserId, User authUser){
        User user = userService.getUserById(requestedUserId);
        Optional<ConnectionRequest> optionalConnectionRequest = connectionRequestRepo.findBySenderIdAndReceiverId(authUser.getId(), user.getId());
        boolean isAutoFollowOn = userPreferenceService.isAutoFollowOn(requestedUserId);
        boolean isNewFollowerNotificationOn = userPreferenceService.isNewFollowerNotificationOn(requestedUserId);
        if(optionalConnectionRequest.isPresent()){
            ConnectionRequest connectionRequest = optionalConnectionRequest.get();
            connectionRequest.setStatus(ConnectionRequestStatus.PENDING);
            connectionRequestRepo.save(connectionRequest);
            if(isAutoFollowOn){
                acceptRequest(user,authUser.getId());
                return;
            }
            if(isNewFollowerNotificationOn) {
                UserDto userDto = userService.getUserDtoByUser(authUser);
                NotificationDto notificationDto = new ConnectionRequestNotificationDto(connectionRequest.getId(), userDto, user.getEmail(), "sent you a connection request", NotificationType.CONNECTION_REQUEST);
                eventPublisher.publishEvent(new SendNotificationEvent(this, notificationDto));
                return;
            }

        }
        ConnectionRequest connectionRequest = saveConnectionRequest(requestedUserId, authUser.getId());
        if(isAutoFollowOn){
            acceptRequest(user,authUser.getId());
            return;
        }
        if(isNewFollowerNotificationOn) {
            UserDto userDto = userService.getUserDtoByUser(authUser);
            NotificationDto notificationDto = new ConnectionRequestNotificationDto(connectionRequest.getId(), userDto, user.getEmail(), "sent you a connection request", NotificationType.CONNECTION_REQUEST);
            eventPublisher.publishEvent(new SendNotificationEvent(this, notificationDto));
        }
    }

    

    public List<ConnectionRequestNotificationDto> getAllPendingConnectionRequest(String authToken){
        User authUser = userService.getAuthUser(authToken);
        return connectionRequestRepo.findByReceiverIdAndStatus(authUser.getId(),ConnectionRequestStatus.PENDING).stream().map(this::entityToPendingConnectionRequestDto).collect(Collectors.toList());
    }

    public boolean isConnectionRequestPending(String receiverId,String senderId){
       return connectionRequestRepo.existsBySenderIdAndReceiverIdAndStatus(senderId,receiverId,ConnectionRequestStatus.PENDING);
    }

    public boolean isAlreadyExistConnectionRequest(String requestId,String authUserId){
        return connectionRequestRepo.findBySenderIdAndReceiverId(authUserId,requestId).isPresent();
    }

    public void unfollow(String userId,String token){
        User authUser = userService.getAuthUser(token);
        boolean existsByIdAndFollowerContains = userConnectionRepo.existsByIdAndFollowerContains(userId, authUser.getId());
        if (existsByIdAndFollowerContains){
            Optional<ConnectionRequest> optionalConnectionRequest = connectionRequestRepo.findBySenderIdAndReceiverId(userId, authUser.getId());
            if(optionalConnectionRequest.isPresent()){
                ConnectionRequest connectionRequest = optionalConnectionRequest.get();
                connectionRequest.setStatus(ConnectionRequestStatus.REJECTED);
                connectionRequestRepo.save(connectionRequest);
            }
            Optional<UserConnection> optionalUserConnection=userConnectionRepo.findByUserIdAndFollowerId(userId,authUser.getId());
            if(optionalUserConnection.isPresent()){
                UserConnection userConnection = optionalUserConnection.get();
                userConnectionRepo.delete(userConnection);
            }
        }
    }
    
    public ConnectionRequestNotificationDto entityToPendingConnectionRequestDto(ConnectionRequest connectionRequest){
        User senderUser = userService.getUserById(connectionRequest.getSenderId());
        User recieverUser=userService.getUserById(connectionRequest.getReceiverId());
        UserDto userDto = userService.getUserDtoByUser(senderUser);
        return new ConnectionRequestNotificationDto(connectionRequest.getId(),userDto,recieverUser.getEmail(),"sent to a connection request",NotificationType.CONNECTION_REQUEST);
    }

    public ConnectionRequest saveConnectionRequest(String requestId,String authUserId){
        boolean connectionRequestPending = isConnectionRequestPending(requestId, authUserId);
        if(!connectionRequestPending) {
            ConnectionRequest connectionRequest = ConnectionRequest.builder()
                    .senderId(authUserId)
                    .receiverId(requestId)
                    .status(ConnectionRequestStatus.PENDING)
                    .build();
            return connectionRequestRepo.save(connectionRequest);
        }
        throw new RuntimeException("Request already send");
    }

    
    public ConnectionRequest getConnectionRequest(String requestedId,String authUserId){
        return connectionRequestRepo.findBySenderIdAndReceiverId(requestedId,authUserId).orElseThrow(()->new RuntimeException("Not found user connection"));
    }
    
    @Transactional
    public void acceptRequest(User authUser,String requestedUserId){
        ConnectionRequest connectionRequest = getConnectionRequest(requestedUserId, authUser.getId());
        connectionRequest.setStatus(ConnectionRequestStatus.ACCEPTED);
        update(connectionRequest);
        UserConnection userConnection =createNewConnection(authUser.getId(),requestedUserId);
        userConnectionRepo.save(userConnection);
    }

    public void update(ConnectionRequest connectionRequest){
        connectionRequestRepo.save(connectionRequest);
    }

    private UserConnection createNewConnection(String authId,String requestedUserId) {
        boolean existsByIdAndFollowerContains = userConnectionRepo.existsByIdAndFollowerContains(authId, requestedUserId);
        if(existsByIdAndFollowerContains) throw new RuntimeException("user already following");
        return UserConnection.builder()
                .userId(authId)
                .followerId(requestedUserId)
                .build();
    }
    
    public List<UserConnection> getFollowerList(String authId){
        List<UserConnection> userConnectionList = userConnectionRepo.findByUserId(authId);
        if(!userConnectionList.isEmpty()){
            return userConnectionList;
        }
        return Collections.emptyList();
    }

//    public List<String> getFollowerList(String authId,Integer pageNum){
//        Optional<UserConnection> userConnectionOptional = userConnectionRepo.findByUserId(authId);
//        if(userConnectionOptional.isPresent()){
//            List<String> follower = userConnectionOptional.get().getFollower();
//            int followerSize=follower.size();
//            int totalPage=followerSize/DEFAULT_CAPACITY_FOR_PAGE_SIZE;
//            return follower.subList(pageNum,)
//        }
//        return Collections.emptyList();
//    }

    public List<UserConnection> getFollowingList(String authId){
        List<UserConnection> connections = userConnectionRepo.findByFollowerId(authId);
        if(!connections.isEmpty()){
            return connections;
        }
        return Collections.emptyList();
    }
//
//    public List<String> getFollowingList(String authId,Integer pageNum){
//        Optional<UserConnection> userConnectionOptional = userConnectionRepo.findByUserId(authId);
//        if(userConnectionOptional.isPresent()){
//            return userConnectionOptional.get().getFollowing();
//        }
//        return Collections.emptyList();
//    }

    public void rejectFriendRequest(String userId,String token){
        User authUser = userService.getAuthUser(token);
        User user = userService.getUserById(userId);
        ConnectionRequest connectionRequest = getConnectionRequest(user.getId(), authUser.getId());
        connectionRequest.setStatus(ConnectionRequestStatus.REJECTED);
        update(connectionRequest);
    }
    
    public List<UserConnectionDTO> getUserChatHistory(String token){
        User authUser = userService.getAuthUser(token);
        List<RecentChatHistory> recentChatHistoryList = chatRoomService.getRecentChatHistory(authUser.getId());
        return recentChatHistoryList.stream().map(recentChatHistory -> this.entityToUserConnectionDTO(recentChatHistory.getUserId(),authUser)).collect(Collectors.toList());
    }


    public boolean existsByIdAndFollowerContains(String authId,String userId){
        return userConnectionRepo.existsByIdAndFollowerContains(authId,userId);
    }
    public boolean existsByIdAndFollowingContains(String authId,String userId){
        return userConnectionRepo.existsByIdAndFollowingContains(authId,userId);
    }
    
    public List<UserConnectionDTO> getFollowerAndFollowing(String authToken){
        User authUser = userService.getAuthUser(authToken);
        List<UserConnection> followerList = getFollowerList(authUser.
                getId());
        List<UserConnection> followingList = getFollowingList(authUser.getId());
        Set<UserConnectionDTO> userDtoList=new HashSet<>();
        List<UserConnectionDTO> followerUserDTO = followerList.stream().map(userConnection -> this.entityToUserConnectionDTO(userConnection.getFollowerId(),authUser)).toList();
        List<UserConnectionDTO> followingUserDTO = followingList.stream().map(userConnection -> this.entityToUserConnectionDTO(userConnection.getUserId(),authUser)).toList();
        userDtoList.addAll(followingUserDTO);
        userDtoList.addAll(followerUserDTO);
        return new ArrayList<>(userDtoList);
    }

    public void removeFriendShip(String userId,String token){
        User authUser = userService.getAuthUser(token);
        Optional<UserConnection> optionalUserConnection = userConnectionRepo.findByUserIdAndFollowerId(authUser.getId(), userId);
        if(optionalUserConnection.isPresent()){
            UserConnection userConnection = optionalUserConnection.get();
            Optional<ConnectionRequest> optionalConnectionRequest = connectionRequestRepo.findBySenderIdAndReceiverId(userId, authUser.getId());
            if(optionalConnectionRequest.isPresent()){
                ConnectionRequest connectionRequest = optionalConnectionRequest.get();
                connectionRequest.setStatus(ConnectionRequestStatus.REJECTED);
                connectionRequestRepo.save(connectionRequest);
            }
            userConnectionRepo.delete(userConnection);
        }
    }
    
    public Page<UserConnectionDTO> getFollowing(String authToken,Integer pageNum){
        User authUser = userService.getAuthUser(authToken);
        PageRequest pageRequest = PageRequest.of(pageNum, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.DESC, "createdAt");
        return userConnectionRepo.findByFollowerId(authUser.getId(), pageRequest).map(userConnection -> entityToUserConnectionDTO(userConnection.getUserId(), authUser));
    }

    public List<UserConnectionDTO> getFollowing(String otherUserId,String authToken){
        User authUser = userService.getAuthUser(authToken);
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userConnectionRepo.findByFollowerId(otherUserId, pageRequest).map(userConnection -> entityToUserConnectionDTO(userConnection.getUserId(), authUser)).toList();
    }

    public Page<UserConnectionDTO> getFollower(String authToken,Integer pageNum){
        User authUser = userService.getAuthUser(authToken);
        PageRequest pageRequest = PageRequest.of(pageNum, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.DESC, "createdAt");
        return userConnectionRepo.findByUserId(authUser.getId(), pageRequest).map(userConnection -> entityToUserConnectionDTO(userConnection.getFollowerId(), authUser));
    }

    public List<UserConnectionDTO> getFollower(String otherUserId,String authToken){
        User authUser = userService.getAuthUser(authToken);
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userConnectionRepo.findByUserId(otherUserId,pageRequest).map(userConnection -> entityToUserConnectionDTO(userConnection.getFollowerId(), authUser)).toList();
    }

    public Page<UserConnectionDTO> getUserConnection(String type,String authToken,Integer pageNum){
        type=type.trim().toLowerCase();
        if(type.equals("follower")) return getFollower(authToken,pageNum);
        return getFollowing(authToken,pageNum);
    }

    public List<UserConnectionDTO> getOtherUserConnection(String otherUserId,String type,String authToken){
        type=type.trim().toLowerCase();
        if(type.equals("follower")) return getFollower(otherUserId,authToken);
        return getFollowing(otherUserId,authToken);
    }

    private UserConnectionDTO entityToUserConnectionDTO(String userId,User authUser){
        User user = userService.getUserById(userId);
        boolean followerContains = existsByIdAndFollowerContains(user.getId(), authUser.getId());
        return UserConnectionDTO.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .profileURL(user.getProfileURL())
                .isRequestPending(isConnectionRequestPending(user.getId(),authUser.getId()))
                .isAuthUserFollow(followerContains).build();
    }

    public void deleteUserFromAllConnections(String id) {
        userConnectionRepo.deleteByUserId(id);
        userConnectionRepo.deleteByFollowerId(id);
    }

    public Integer getFollowerCount(String userId){
        return userConnectionRepo.findByUserId(userId).size();
    }


    public Integer getFollowingCount(String userId){
        return userConnectionRepo.findByFollowerId(userId).size();
    }

}
