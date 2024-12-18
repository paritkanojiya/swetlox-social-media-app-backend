package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.ProfileModel;
import com.swetlox_app.swetlox.service.ForgetPasswordService;
import com.swetlox_app.swetlox.service.UserConnectionService;
import com.swetlox_app.swetlox.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/user")
public class UserController {

    private final UserService userService;
    private final UserConnectionService userConnectionService;
    private final ForgetPasswordService forgetPasswordService;
    private final ModelMapper mapper;

    @GetMapping("/following-request/{id}")
    public ResponseEntity<AuthResponse> followingRequest(@PathVariable("id") String followingId, @RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userConnectionService.following(followingId,authUser);
        AuthResponse authResponse = AuthResponse.builder()
                .message("request send")
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(authResponse);
    }
    @GetMapping("/get-user/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        User user = userService.getUser(email);
        UserDto userDto = UserDto.builder()
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileURL(user.getProfileURL())
                .build();
        return ResponseEntity.ok(userDto);
    }
    @GetMapping("/acceptRequest/{id}")
    public ResponseEntity<?> acceptRequest(@PathVariable("id") String requestedUserId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userConnectionService.acceptRequest(authUser,requestedUserId);
        return ResponseEntity.ok("request accepted");
    }

    @GetMapping("/get-follower")
    public ResponseEntity<List<String>> getFollower(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<String> followerList = userConnectionService.getFollowerList(authUser.getId());
        return ResponseEntity.ok(followerList);
    }

    @GetMapping("/get-following")
    public ResponseEntity<List<String>> getFollowing(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<String> followerList = userConnectionService.getFollowingList(authUser.getId());
        return ResponseEntity.ok(followerList);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute User user,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        if(!user.getUserName().isEmpty()){
            authUser.setUserName(user.getUserName());
        }
        if(!user.getBio().isEmpty()){
            authUser.setBio(user.getBio());
        }
        userService.updateUser(authUser);
        return ResponseEntity.ok("updated");
    }
    @GetMapping("/auth-status")
    public ResponseEntity<UserDto> getAuthStatus(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        UserDto userDto = mapper.map(authUser, UserDto.class);
        userDto.setPassword(null);
        return ResponseEntity.ok(userDto);
    }
    
    @PostMapping("/change-profile-image")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDto> changeProfileImage(@RequestPart("file")MultipartFile multipartFile,@RequestHeader("Authorization") String token) throws IOException {
        User authUser = userService.getAuthUser(token);
        UserDto userDto = userService.changeProfileImage(multipartFile, authUser);
        return ResponseEntity.ok(userDto);
    }
    
    @GetMapping("/profile-data")
    public ResponseEntity<ProfileModel> getProfileData(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        ProfileModel profileModel = userService.profileData(authUser);
        return ResponseEntity.ok(profileModel);
    }
    
    @GetMapping("/search-user")
    public ResponseEntity<List<Map<String,Object>>> getSearchUser(@RequestParam("query") String query,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<Map<String, Object>> searchedUser = userService.searchUser(query,authUser);
        return ResponseEntity.ok(searchedUser);
    }

    @GetMapping("/get-user-connection")
    public ResponseEntity<List<UserDto>> getUserConnection(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<UserDto> userConnection = userService.findUserConnection(authUser.getId());
        return ResponseEntity.ok(userConnection);
    }

}
