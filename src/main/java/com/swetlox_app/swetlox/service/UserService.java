package com.swetlox_app.swetlox.service;


import com.cloudinary.Cloudinary;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.model.ProfileModel;
import com.swetlox_app.swetlox.repository.StoryRepo;
import com.swetlox_app.swetlox.repository.UserRepo;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService otpService;
    private final JwtService jwtService;
    private final Cloudinary cloudinaryTemplate;
    private final ModelMapper mapper;
    @Lazy
    @Autowired
    private  StoryService storyService;

    @Lazy
    @Autowired
    private  PostService postService;
    @Lazy
    @Autowired
    private UserConnectionService userConnectionService;

    @Scheduled(fixedRate = 60000)
    public void deleteNotVerifyUser(){
        LocalDateTime tenMinutesAgo=LocalDateTime.now().minusMinutes(10);
        List<User> deletedUser = userRepository.findByIsVerifiedFalseAndCreatedAtBefore(tenMinutesAgo);
        userRepository.deleteAll(deletedUser);
    }
    
//    @PostConstruct
//    public void loadAdmin(){
//        Role role=new Role();
//        role.setRole("ADMIN");
//        User adminUser = User.builder()
//                .userName("admin")
//                .email("admin@gmail.com")
//                .password(passwordEncoder.encode("parit2003"))
//                .isVerified(true)
//                .createdAt(LocalDateTime.now())
//                .roleList(List.of(role))
//                .build();
//        userRepository.save(adminUser);
//    }
    
    public User getUser(String email){
        return userRepository.findByEmail(email);
    }

    public User saveUser(UserDto userDto) throws MessagingException, UserAlreadyExistEx {
        User isAlreadyExits = userRepository.findByEmail(userDto.getEmail());
        if(isAlreadyExits==null){
            User user = User.builder().userName(userDto.getUserName())
                    .fullName(userDto.getFullName())
                    .email(userDto.getEmail())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .roleList(List.of(new Role("ROLE_USER")))
                    .isVerified(false)
                    .profileURL("https://res.cloudinary.com/dkbbhmnk6/image/upload/v1724336510/default_ia7jfs.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();
            otpService.sendOtp(user.getEmail());
            return userRepository.save(user);
        }
        if(Boolean.FALSE.equals(isAlreadyExits.getIsVerified())){
            otpService.sendOtp(isAlreadyExits.getEmail());
            return isAlreadyExits;
        }
        throw new UserAlreadyExistEx(userDto.getEmail()+ " user already present");
    }

    public void saveOAut2User(UserDto user){
        User isExist = userRepository.findByEmail(user.getEmail());
        if(isExist==null) {
            User oAuth2User = User.builder()
                    .userName(user.getUserName())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roleList(user.getRoleList())
                    .createdAt(LocalDateTime.now())
                    .isVerified(true)
                    .profileURL("https://res.cloudinary.com/dkbbhmnk6/image/upload/v1724336510/default_ia7jfs.jpg")
                    .isOAuth2User(true)
                    .build();
            userRepository.save(oAuth2User);
        }
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

    public User getUserById(String id){
        System.out.println(id);
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("user not found"));
    }

    public User getAuthUser(String token){
        String userEmail = jwtService.extractUserNameFromToken(token.substring(7));
        return userRepository.findByEmail(userEmail);
    }

    public List<Map<String, Object>> searchUser(String q, User authUser) {
        List<Map<String, Object>> users = new ArrayList<>();
        if (q.isEmpty()) {
            return Collections.emptyList();
        } else {
            userRepository.findByUserName(q).forEach(user -> {
                boolean followerContainsUserId = userConnectionService.existsByIdAndFollowerContains(authUser.getId(), user.getId());
                boolean followingContainsUserId = userConnectionService.existsByIdAndFollowingContains(authUser.getId(), user.getId());
                boolean userIsFollowingAuth = userConnectionService.existsByIdAndFollowingContains(user.getId(), authUser.getId());

                Map<String, Object> userMap = Map.of(
                        "userId", user.getId(),
                        "userName", user.getUserName(),
                        "follower", followerContainsUserId,
                        "following", followingContainsUserId,
                        "userIsFollowingAuth", userIsFollowingAuth,
                        "profileURL",user.getProfileURL()
                );
                users.add(userMap);
            });
        }
        return users;
    }


    public UserDto changeProfileImage(MultipartFile file,User authUser) throws IOException {
        Map uploaded = cloudinaryTemplate.uploader().upload(file.getBytes(), Collections.emptyMap());
        String profileURL=(String)uploaded.get("url");
        authUser.setProfileURL(profileURL);
        User updatedUser = userRepository.save(authUser);
        return mapper.map(updatedUser, UserDto.class);
    }

    public ProfileModel profileData(User authUser){
        List<String> followerList = userConnectionService.getFollowerList(authUser.getId());
        List<String> followingList = userConnectionService.getFollowingList(authUser.getId());
        Integer userPostCount = postService.getUserPostCount(authUser.getId());

        int follower = (followerList != null) ? followerList.size() : 0;
        int following = (followingList != null) ? followingList.size() : 0;

        return ProfileModel.builder().userName(authUser.getUserName())
                .fullName(authUser.getFullName())
                .profileURL(authUser.getProfileURL())
                .follower(follower)
                .following(following)
                .bio(authUser.getBio())
                .postCount(userPostCount)
                .build();
    }

    public List<UserDto> findUserConnection(String authUser){
        return userConnectionService.getFollowerAndFollowing(authUser);
    }

    public long getNumOfUser(){
        return userRepository.count();
    }
    
    public Page<User> getAllUser(Integer pageNum){
        PageRequest pageRequest = PageRequest.of(pageNum, 5);
        return userRepository.findAll(pageRequest);
    }
    
    public void deleteUser(String id){
        User user = getUserById(id);
        userRepository.delete(user);
        postService.deleteAllPostByUserId(id);
        storyService.deleteStoryByUserId(id);
        userConnectionService.deleteUserFromAllConnections(id);
    }
}

