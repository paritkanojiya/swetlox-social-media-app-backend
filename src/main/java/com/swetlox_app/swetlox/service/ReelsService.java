package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.notification.InteractionNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.reel.ReelResponseDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.dto.usercollection.UserCollectionDto;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.model.ReelsModel;
import com.swetlox_app.swetlox.repository.ReelsRepo;
import com.swetlox_app.swetlox.repository.UserCollectionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReelsService {

    private final ReelsRepo reelsRepo;
    private final ModelMapper mapper;
    private final MongoTemplate mongoTemplate;
    private final CloudService cloudService;
    @Autowired
    @Lazy
    private  UserService userService;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final CommentService commentService;
    private final UserConnectionService userConnectionService;
    private final UserCollectionRepo userCollectionRepo;
    private final UserPreferenceService userPreferenceService;

    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;

    public void saveReel(User authUser, MultipartFile file,String caption) throws IOException {

        Map uploaded = cloudService.upload(file, MediaType.VIDEO);
        String absoluteUrl = (String) uploaded.get("url");
        Reels reels = Reels.builder().userId(authUser.getId())
                .caption(caption)
                .createdAt(LocalDateTime.now())
                .reelsURL(absoluteUrl)
                .build();
        reelsRepo.save(reels);
    }


    private ReelResponseDto convertReelsToReelResponseDto(Reels reels,User authUser){
        boolean like = likeService.isLike(reels.getId(), authUser.getId());
        long likeCount = likeService.countLikeEntityByEntityIdAndEntityType(reels.getId(), EntityType.REEL);
        boolean isBookMark = isBookMark(reels.getId(), authUser.getId());
        UserDto userDto = userService.getUserDtoById(reels.getUserId());
        boolean isFollow = userConnectionService.existsByIdAndFollowingContains(userDto.getUserId(), authUser.getId());

        return ReelResponseDto.builder()
                .createdAt(reels.getCreatedAt())
                .reelURL(reels.getReelsURL())
                .postUser(userDto)
                .reelId(reels.getId())
                .caption(reels.getCaption())
                .isLike(like)
                .likeCount((int) likeCount)
                .isFollow(isFollow)
                .isBookMark(isBookMark)
                .build();
    }

    private boolean isBookMark(String reelId,String userId){
        return userCollectionRepo.findByEntityIdAndUserIdAndBookMark(reelId,userId,true).isPresent();
    }

    public Page<ReelResponseDto> getReelByUserId(String userId,Integer pageNum,String token){
        User authUser = userService.getAuthUser(token);
        PageRequest pageRequest=PageRequest.of(pageNum,DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.ASC,"createdAt");
        Page<Reels> reelsPage = reelsRepo.findByUserId(userId, pageRequest);
        List<ReelResponseDto> reelResponseDtos = reelsPage.map(reels -> convertReelsToReelResponseDto(reels, authUser)).toList();
        return new PageImpl<>(reelResponseDtos,pageRequest,reelsPage.getTotalElements());
    }

    public void deleteReels(String id,String authId){
        Reels reels = reelsRepo.deleteByIdAndUserId(id, authId);
        userCollectionRepo.findByEntityIdAndBookMark(reels.getId(),true).orElse(new ArrayList<>()).stream().forEach(userCollections -> {userCollections.setBookMark(false); userCollectionRepo.save(userCollections);});
    }

    public int getTotalReelCount(){
        return (int) reelsRepo.count();
    }

    public int getUserReelCount(String userId){
        return reelsRepo.countByUserId(userId);
    }
    public void likedReels(String reelId,String userId){
        Optional<Reels> optionalReels = reelsRepo.findById(reelId);
        if(optionalReels.isEmpty()) throw new RuntimeException("reel not found");
        Reels reels = optionalReels.get();
        UserDto userDto = userService.getUserDtoById(userId);
        User user = userService.getUserById(reels.getUserId());
        Optional<Like> optionalLike = likeService.isExist(reelId, userId);
        if(optionalLike.isPresent()){
            Like like = optionalLike.get();
            if(like.isLiked()){
                like.setLiked(false);
                likeService.update(like);
                return;
            }else{
                like.setLiked(true);
                likeService.update(like);
                boolean isLikeCommentNotificationOn = userPreferenceService.isOnLikeCommentNotification(reels.getUserId());
                if(isLikeCommentNotificationOn){
                    NotificationDto notificationDto=new InteractionNotificationDto(UUID.randomUUID().toString(),userDto,user.getEmail(),"like on your reel", NotificationType.REEL,null);
                    notificationService.sendNotification(notificationDto);
                    return;
                }
            }

        }
        Like like = Like.builder()
                .liked(true)
                .userId(userId)
                .entityType(EntityType.REEL)
                .entityId(reelId)
                .build();
        likeService.save(like);
        boolean isLikeCommentNotificationOn = userPreferenceService.isOnLikeCommentNotification(reels.getUserId());
        if(isLikeCommentNotificationOn) {
            NotificationDto notificationDto = new InteractionNotificationDto(UUID.randomUUID().toString(), userDto, user.getEmail(), "like on your reel", NotificationType.REEL, null);
            notificationService.sendNotification(notificationDto);
        }
    }


    public List<CommentResponseDto> getCommentList(String reelId){
       return commentService.getCommentListByEntityId(reelId);
    }
    public List<ReelResponseDto> userReels(String authId) {
        User authUser = userService.getUserById(authId);
        return reelsRepo.findByUserId(authId).stream().map(reels -> convertReelsToReelResponseDto(reels,authUser)).collect(Collectors.toList());
    }

    public ReelResponseDto getReelResponseById(String reelId,String token){
        User authUser = userService.getAuthUser(token);
        return reelsRepo.findById(reelId).map(reels -> convertReelsToReelResponseDto(reels,authUser)).orElseThrow(()->new RuntimeException("reel not found"));
    }


    public Page<ReelResponseDto> loadReels(Integer pageNum,String token){
        User authUser = userService.getAuthUser(token);
        Pageable pageRequest= PageRequest.of(pageNum,DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.ASC,"createdAt");
        Query connectionQuery = new Query(Criteria.where("followerId").is(authUser.getId()));
        List<String> followedUserIds = new ArrayList<>(mongoTemplate.find(connectionQuery, UserConnection.class)
                .stream()
                .map(UserConnection::getUserId)
                .toList());

        // Add the authenticated user's ID to the list of followed users
        followedUserIds.add(authUser.getId());
        Criteria publicReels = Criteria.where("privateReel").is(false);
        Criteria privateReels = Criteria.where("userId").in(followedUserIds);

        // Combine criteria
        Query reelQuery = new Query(new Criteria().orOperator(publicReels, privateReels))
                .with(pageRequest);

        // Step 3: Fetch posts
        List<Reels> reelsList = mongoTemplate.find(reelQuery, Reels.class);
        List<ReelResponseDto> reelResponseDtos = reelsList.stream().map((reel) -> convertReelsToReelResponseDto(reel, authUser)).toList();
        long total = mongoTemplate.count(reelQuery.skip(-1).limit(-1), Reels.class);

        return new PageImpl<>(reelResponseDtos,pageRequest,total);
    }


    private void removeUserIdFromLikedUserList(String reelId, String userId) {
        Query query = new Query(Criteria.where("_id").is(reelId));
        Update update = new Update().pull("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    private void addUserIdFromLikedUserList(String reelId, String userId) {
        Query query = new Query(Criteria.where("_id").is(reelId));
        Update update = new Update().addToSet("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }


    public void bookMarkReel(String reelId,String token){
        User authUser = userService.getAuthUser(token);
        Optional<UserCollection> optionalUserCollection = userCollectionRepo.findByEntityIdAndUserId(reelId, authUser.getId());
        if(optionalUserCollection.isPresent()){
            UserCollection userCollection = optionalUserCollection.get();
            userCollection.setBookMark(!userCollection.isBookMark());
            updateUserCollection(userCollection);
            return;
        }
        UserCollection userCollection = UserCollection.builder()
                .bookMark(true)
                .userId(authUser.getId())
                .entityId(reelId)
                .entityType(EntityType.REEL)
                .build();
        saveUserCollection(userCollection);
    }

    public List<UserCollectionDto> getBookMarkReels(String token){
        User authUser = userService.getAuthUser(token);
        List<UserCollection> userCollections = userCollectionRepo.findByUserIdAndBookMarkAndEntityType(authUser.getId(), true, EntityType.REEL).orElse(new ArrayList<>());
        return userCollections.stream().map(this::userCollectionDto).toList();
    }

    public UserCollectionDto userCollectionDto(UserCollection userCollection){
        String entityId = userCollection.getEntityId();
        Reels reel = getReelById(entityId);
        return UserCollectionDto.builder().id(userCollection.getId())
                .id(userCollection.getId())
                .entityId(userCollection.getEntityId())
                .savedAt(userCollection.getCreatedAt())
                .caption(reel.getCaption())
                .entityType(EntityType.REEL)
                .entityURL(reel.getReelsURL())
                .build();
    }

    public void updateUserCollection(UserCollection userCollection){
        userCollectionRepo.save(userCollection);
    }

    public void saveUserCollection(UserCollection userCollection){
        userCollectionRepo.save(userCollection);
    }

    public Reels getReelById(String id){
        return reelsRepo.findById(id).orElseThrow(()->new RuntimeException("reel not found"));
    }

    public void comment(CommentRequestDto commentRequestDto, String token) {
        User authUser = userService.getAuthUser(token);
        Reels reel = getReelById(commentRequestDto.getEntityId());
        User user = userService.getUserById(reel.getUserId());
        commentService.saveComment(commentRequestDto,authUser,user);
    }

    public void deleteAllReelsByUserId(String id) {
        List<Reels> reelsList = reelsRepo.findByUserId(id);
        reelsList.stream().parallel().filter(post -> userCollectionRepo.existsByEntityId(post.getId())).forEach(post -> {
            UserCollection userCollection=userCollectionRepo.findByEntityId(post.getId());
            userCollection.setBookMark(false);
            userCollectionRepo.save(userCollection);
        });
        reelsList.forEach(reel -> likeService.deleteByEntityId(reel.getId()));
        reelsList.forEach(reel -> commentService.deletePostComment(reel.getId()));
        reelsRepo.deleteByUserId(id);
    }
}
