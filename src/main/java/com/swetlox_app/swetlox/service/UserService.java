package com.swetlox_app.swetlox.service;


import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.model.ProfileModel;
import com.swetlox_app.swetlox.repository.RecentChatHistoryRepo;
import com.swetlox_app.swetlox.repository.UserCollectionRepo;
import com.swetlox_app.swetlox.repository.UserRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService otpService;
    private final JwtService jwtService;
    private final CloudService cloudService;
    private final ModelMapper mapper;
    private final UserPreferenceService userPreferenceService;
    @Lazy
    @Autowired
    private  ReelsService reelsService;
    @Lazy
    @Autowired
    private final LikeService likeService;
    @Lazy
    @Autowired
    private CommentService commentService;
    @Lazy
    @Autowired
    private UserCollectionRepo userCollectionRepo;
    @Autowired
    @Lazy
    private RecentChatHistoryRepo recentChatHistoryRepo;
    @Autowired
    @Lazy
    private MessageService messageService;
    @Autowired
    @Lazy
    private ChatRoomService chatRoomService;
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

    public User getUser(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("provided user email is not found"));
    }
    
    public UserDetailsDto getUserDetailsDto(String token){
        User authUser = getAuthUser(token);
        UserPreference userPreference = userPreferenceService.getUserPreferenceByUserId(authUser.getId());
        System.out.println(userPreference);
        return UserDetailsDto.builder().userType(authUser.getUserType())
                .id(authUser.getId())
                .roleList(authUser.getRoleList())
                .password(null)
                .bio(authUser.getBio())
                .email(authUser.getEmail())
                .fullName(authUser.getFullName())
                .userName(authUser.getUserName())
                .profileURL(authUser.getProfileURL())
                .userType(authUser.getUserType())
                .userPreference(userPreference)
                .build();
    }
    
    public User saveUser(UserDetailsDto userDto) throws UserAlreadyExistEx, MessagingException {
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        String userName = userDto.getUserName();
        boolean isUserNameExist=userRepository.existsByUserName(userName);
        if(optionalUser.isEmpty()){
            User user = toEntity(userDto);
            if(isUserNameExist) throw new RuntimeException("username already exist");
            user.setUserType(UserType.EMAIL);
            otpService.sendOtp(user.getEmail());
            User savedUser = userRepository.save(user);
            userPreferenceService.initialSetting(savedUser.getId());
            return savedUser;
        }
        User user = optionalUser.get();
        boolean suspense = user.isSuspense();
        if(suspense) throw new RuntimeException("you are blacklist user");
        boolean isSame = user.getUserName().equals(userDto.getUserName());
        if(!isSame){
            if(isUserNameExist) throw new RuntimeException("username already exist");
        }
        if(Boolean.FALSE.equals(user.getIsVerified())){
            otpService.sendOtp(user.getEmail());
            return user;
        }
        throw new UserAlreadyExistEx(userDto.getEmail()+ " user already present");
    }
    
    public User saveOAuth2User(UserDetailsDto userDto){
        User user = toEntity(userDto);
        user.setIsVerified(true);
        user.setSuspense(false);
        User savedUser = userRepository.save(user);
        userPreferenceService.initialSetting(savedUser.getId());
        return savedUser;
    }
    
    
    public void changeVerificationStatus(String email){
        User user = getUser(email);
        if(!user.getIsVerified()){
            user.setIsVerified(true);
            updateUser(user);
        }
    }

    private User toEntity(UserDetailsDto userDto){
        String encodedPassword=userDto.getPassword()!=null ? passwordEncoder.encode(userDto.getPassword()) : null;
        return User.builder().userName(userDto.getUserName())
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .password(encodedPassword)
                .roleList(List.of(new Role("ROLE_USER")))
                .userType(userDto.getUserType())
                .suspense(false)
                .isVerified(false)
                .profileURL("https://res.cloudinary.com/dkbbhmnk6/image/upload/v1724336510/default_ia7jfs.jpg")
                .build();
    }

    public void updatePassword(User user,String rawNewPassword){
        user.setPassword(passwordEncoder.encode(rawNewPassword));
        userRepository.save(user);
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

    public void updateProfile(User user,String token){
        User authUser = getAuthUser(token);
        if(!user.getUserName().isEmpty()){
            boolean isSame = user.getUserName().equals(authUser.getUserName());
            if(!isSame){
                boolean isUserNameExist = userRepository.existsByUserName(user.getUserName());
                if(isUserNameExist) throw new RuntimeException("username already exist");
                authUser.setUserName(user.getUserName());
            }
        }
        if(!user.getBio().isEmpty()){
            authUser.setBio(user.getBio());
        }
        userRepository.save(authUser);
    }

    public User getUserById(String id){
        
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("user not found"));
    }

    public User getAuthUser(String token){
        String userEmail = jwtService.extractUserNameFromToken(token.substring(7));
        return userRepository.findByEmail(userEmail).orElseThrow(()-> new RuntimeException("User not found"));
    }

    public boolean isUserExistById(String id){return userRepository.existsById(id);}
    public boolean isUserExistByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public List<Map<String, Object>> searchUser(String q, User authUser) {
        List<Map<String, Object>> users = new ArrayList<>();
        if (q.isEmpty()) {
            return Collections.emptyList();
        } else {
            userRepository.findByUserName(q).forEach(user -> {
                boolean followerContainsUserId = userConnectionService.existsByIdAndFollowerContains(authUser.getId(), user.getId());
                boolean isRequestPending=userConnectionService.isConnectionRequestPending(user.getId(),authUser.getId());
                boolean followingContainsUserId = userConnectionService.existsByIdAndFollowingContains(authUser.getId(),user.getId());
                boolean userIsFollowingAuth = userConnectionService.existsByIdAndFollowingContains(user.getId(),authUser.getId());
                Map<String, Object> userMap = Map.of(
                        "userId", user.getId(),
                        "email",user.getEmail(),
                        "userName", user.getUserName(),
                        "follower", followerContainsUserId,
                        "requested",isRequestPending,
                        "following", followingContainsUserId,
                        "userIsFollowingAuth", userIsFollowingAuth,
                        "profileURL",user.getProfileURL()
                );
                users.add(userMap);
            });
        }
        return users;
    }


    public UserDetailsDto changeProfileImage(MultipartFile file, User authUser) throws IOException {
        Map uploaded = cloudService.upload(file, MediaType.IMAGE);
        String profileURL=(String)uploaded.get("url");
        authUser.setProfileURL(profileURL);
        User updatedUser = userRepository.save(authUser);
        return mapper.map(updatedUser, UserDetailsDto.class);
    }

    public ProfileModel profileData(String otherUserId,String authToken){
        User authUser = getAuthUser(authToken);
        boolean isAuthUserEmail= authUser.getId().equals(otherUserId);
        if(isAuthUserEmail){
            return getSelfProfile(authUser);
        }
        return getOtherUserProfile(authUser,otherUserId);
    }

    private ProfileModel getOtherUserProfile(User authUser, String otherUserId) {
        User otherUser = getUserById(otherUserId);
        return profileEntityToDTO(
                otherUserId,
                otherUser.getFullName(),
                otherUser.getUserName(),
                otherUser.getProfileURL(),
                userConnectionService.getFollowerCount(otherUser.getId()),
                userConnectionService.getFollowingCount(otherUser.getId()),
                postService.getUserPostCount(otherUser.getId()),
                otherUser.getBio(),
                userConnectionService.existsByIdAndFollowingContains(authUser.getId(), otherUser.getId()),
                false,
                userPreferenceService.isPrivateAccount(otherUserId)
        );
    }

    private ProfileModel getSelfProfile(User authUser) {
        return profileEntityToDTO(
                authUser.getId(),
                authUser.getFullName(),
                authUser.getUserName(),
                authUser.getProfileURL(),
                userConnectionService.getFollowerCount(authUser.getId()),
                userConnectionService.getFollowingCount(authUser.getId()),
                postService.getUserPostCount(authUser.getId()),
                authUser.getBio(),
                true,
                true,
                true
        );
    }

    
    
    public ProfileModel profileEntityToDTO(String userId,String fullName, String userName, String profileURL, Integer follower, Integer following, Integer postCount, String bio, boolean isAuthUserFollow, boolean isSelfUser,boolean isPrivateProfile){
        return ProfileModel.builder().fullName(fullName)
                .userId(userId)
                .isAuthUserFollow(isAuthUserFollow)
                .postCount(postCount)
                .bio(bio)
                .isSelfUser(isSelfUser)
                .profileURL(profileURL)
                .follower(follower)
                .following(following)
                .userName(userName)
                .isPrivateProfile(isPrivateProfile)
                .build();
    }

    public UserDto getUserDtoById(String userId){
        User user = getUserById(userId);
        return UserDto.builder().userId(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .profileURL(user.getProfileURL()).build();
    }

    public UserDto getUserDtoByUser(User user){
        return UserDto.builder().userId(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .profileURL(user.getProfileURL()).build();
    }
//    public List<UserDto> findUserConnection(String authUser){
//        return userConnectionService.getFollowerAndFollowing(authUser);
//    }

    public long getNumOfUser(){
        return userRepository.count();
    }
    
    public Page<User> getAllUser(Integer pageNum){
        PageRequest pageRequest = PageRequest.of(pageNum, 5);
        return userRepository.findAllNotAdmin(pageRequest);

    }
    
    public void deleteUser(String id){
        User user = getUserById(id);
        userRepository.delete(user);
        postService.deleteAllPostByUserId(id);
        storyService.deleteStoryByUserId(id);
        userConnectionService.deleteUserFromAllConnections(id);
    }

    public int getPastWeekNewUserCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        return userRepository.countByCreatedAtBetween(sevenDaysAgo, now);
    }

    public void deleteAccountPermanentByToken(String token) {
        User authUser = getAuthUser(token);
        userPreferenceService.deleteByUserId(authUser.getId());
        postService.deleteAllPostByUserId(authUser.getId());
        reelsService.deleteAllReelsByUserId(authUser.getId());
        likeService.deleteAllLikeByUserId(authUser.getId());
        commentService.deleteAllCommentByUserId(authUser.getId());
        userConnectionService.deleteUserFromAllConnections(authUser.getId());
        storyService.deleteStoryByUserId(authUser.getId());
        userCollectionRepo.deleteByUserId(authUser.getId());
        recentChatHistoryRepo.deleteByUserId(authUser.getId());
        recentChatHistoryRepo.deleteByAuthUserId(authUser.getId());
        messageService.deleteAllMessageByUserId(authUser.getId());
        userRepository.delete(authUser);
    }

    public void deleteAccountPermanentByUserId(String userId){
        postService.deleteAllPostByUserId(userId);
        reelsService.deleteAllReelsByUserId(userId);
        likeService.deleteAllLikeByUserId(userId);
        commentService.deleteAllCommentByUserId(userId);
        userConnectionService.deleteUserFromAllConnections(userId);
        storyService.deleteStoryByUserId(userId);
        userCollectionRepo.deleteByUserId(userId);
        recentChatHistoryRepo.deleteByUserId(userId);
        recentChatHistoryRepo.deleteByAuthUserId(userId);
        messageService.deleteAllMessageByUserId(userId);
        userRepository.deleteById(userId);
    }

    public boolean isUserNameExist(String userName){
        return userRepository.existsByUserName(userName);
    }
}

