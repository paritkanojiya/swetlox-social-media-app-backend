package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.AuthResponse;
import com.swetlox_app.swetlox.dto.UserConnectionDTO;
import com.swetlox_app.swetlox.dto.notification.ConnectionRequestNotificationDto;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.ProfileModel;
import com.swetlox_app.swetlox.service.UserConnectionService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserConnectionService userConnectionService;
    private final ModelMapper mapper;

    @GetMapping("/following-request/{id}")
    public ResponseEntity<AuthResponse> followingRequest(@PathVariable("id") String followingId, @RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userConnectionService.followRequest(followingId,authUser);
        AuthResponse authResponse = AuthResponse.builder()
                .message("request send")
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/unfollow/{id}")
    public void unfollowRequest(@PathVariable("id") String userId,@RequestHeader("Authorization") String token){
        userConnectionService.unfollow(userId,token);
    }



    @GetMapping("/remove/friendShip/{id}")
    public void remove(@PathVariable("id") String userId,@RequestHeader("Authorization") String token){
        userConnectionService.removeFriendShip(userId,token);
    }

    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserDetailsDto> getUserByEmail(@PathVariable String id){
        User user = userService.getUserById(id);
        UserDetailsDto userDto = UserDetailsDto.builder()
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileURL(user.getProfileURL())
                .build();
        return ResponseEntity.ok(userDto);
    }


    @GetMapping("/get-all-pending-request")
    public ResponseEntity<List<ConnectionRequestNotificationDto>> getAllPendingConnectionRequest(@RequestHeader("Authorization") String token){
        List<ConnectionRequestNotificationDto> allPendingConnectionRequest = userConnectionService.getAllPendingConnectionRequest(token);
        log.info("pending request {}",allPendingConnectionRequest);
        return ResponseEntity.ok(allPendingConnectionRequest);
    }

    @GetMapping("/acceptRequest/{id}")
    public ResponseEntity<?> acceptRequest(@PathVariable("id") String requestedUserId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userConnectionService.acceptRequest(authUser,requestedUserId);
        return ResponseEntity.ok("request accepted");
    }

    @GetMapping("/rejectRequest/{id}")
    public void rejectRequest(@PathVariable("id") String requestedUserId,@RequestHeader("Authorization") String token){

        userConnectionService.rejectFriendRequest(requestedUserId,token);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String token){
        try {
            userService.deleteAccountPermanentByToken(token);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody User user,@RequestHeader("Authorization") String token){
        try{
            userService.updateProfile(user,token);
            return ResponseEntity.ok("updated");
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }
    @GetMapping("/auth-status")
    public ResponseEntity<UserDetailsDto> getAuthStatus(@RequestHeader("Authorization") String token){
        UserDetailsDto userDetailsDto = userService.getUserDetailsDto(token);
        return ResponseEntity.ok(userDetailsDto);
    }
    
    @PostMapping("/change-profile-image")
    public ResponseEntity<UserDetailsDto> changeProfileImage(@RequestPart("file")MultipartFile multipartFile, @RequestHeader("Authorization") String token) throws IOException {
        User authUser = userService.getAuthUser(token);
        UserDetailsDto userDto = userService.changeProfileImage(multipartFile, authUser);
        return ResponseEntity.ok(userDto);
    }
    
    @GetMapping("/profile-data/{id}")
    public ResponseEntity<ProfileModel> getProfileData(@PathVariable("id") String otherUserId,@RequestHeader("Authorization") String token){
        ProfileModel profileModel = userService.profileData(otherUserId,token);
        System.out.println(profileModel);
        return ResponseEntity.ok(profileModel);
    }

    @GetMapping("/search-user")
    public ResponseEntity<List<Map<String,Object>>> getSearchUser(@RequestParam("query") String query,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<Map<String, Object>> searchedUser = userService.searchUser(query,authUser);
        return ResponseEntity.ok(searchedUser);
    }

    @GetMapping("/get-user-connection")
    public ResponseEntity<List<UserConnectionDTO>> getUserConnection(@RequestHeader("Authorization") String token){
        List<UserConnectionDTO> followerAndFollowing = userConnectionService.getFollowerAndFollowing(token);
        System.out.println(followerAndFollowing);
        return ResponseEntity.ok(followerAndFollowing);
    }

    @GetMapping("/get-user-connection/{type}")
    public ResponseEntity<Page<UserConnectionDTO>> getUserConnection(@PathVariable("type") String type,@RequestParam(value = "pageNum",defaultValue = "0") Integer pageNum,@RequestHeader("Authorization") String token){
        Page<UserConnectionDTO> connectionDTOS = userConnectionService.getUserConnection(type,token, pageNum);
        return ResponseEntity.ok(connectionDTOS);
    }

    @GetMapping("/get-user-connection/{id}/{type}")
    public ResponseEntity<List<UserConnectionDTO>> getOtherUserConnection(@PathVariable String type,@PathVariable String id,@RequestHeader("Authorization") String token){
        List<UserConnectionDTO> otherUserConnection = userConnectionService.getOtherUserConnection(id, type, token);
        return ResponseEntity.ok(otherUserConnection);
    }

    @GetMapping("/get-user-chat-history")
    public ResponseEntity<List<UserConnectionDTO>> getChatHistory(@RequestHeader("Authorization") String token){
        List<UserConnectionDTO> userChatHistory = userConnectionService.getUserChatHistory(token);
        return ResponseEntity.ok(userChatHistory);
    }
}
