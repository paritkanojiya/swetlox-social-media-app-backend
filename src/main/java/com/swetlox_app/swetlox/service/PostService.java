package com.swetlox_app.swetlox.service;

import com.mongodb.client.result.UpdateResult;
import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.notification.InteractionNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.dto.usercollection.UserCollectionDto;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.dto.post.PostResponseDto;
import com.swetlox_app.swetlox.repository.PostRepo;
import com.swetlox_app.swetlox.repository.UserCollectionRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepo postRepo;
    private final CloudService cloudService;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
    private final UserCollectionRepo userCollectionRepo;
    private final UserService userService;
    private final NotificationService notificationService;
    private final CommentService commentService;
    private final UserConnectionService userConnectionService;
    private final UserPreferenceService userPreferenceService;
    private final LikeService likeService;
    
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;
    

    public void savePost(User authUser, MultipartFile file, String caption, boolean visibility) throws IOException {
        Map uploaded = cloudService.upload(file, MediaType.IMAGE);
        String postURL=(String)uploaded.get("url");
        System.out.println(postURL);
        Post createdPost = Post.builder().userId(authUser.getId())
                .caption(caption)
                .createdAt(LocalDateTime.now())
                .postURL(postURL)
                .privatePost(visibility)
                .build();
        postRepo.save(createdPost);
    }

    public void deletePost(String postId,String authId){
        Optional<Post> post=postRepo.findByIdAndUserId(postId,authId);
        if(post.isPresent()){
            Post postGET = post.get();
            postRepo.delete(postGET);
            userCollectionRepo.findByEntityIdAndBookMark(postGET.getId(),true).orElse(new ArrayList<>()).stream().forEach(userCollections -> {userCollections.setBookMark(false); userCollectionRepo.save(userCollections);});
        }
    }

    public void removePostFromUserCollection(String userId, String userCollectionId) {
        Optional<UserCollection> optionalUserCollection = userCollectionRepo.findById(userCollectionId);
        if(optionalUserCollection.isPresent()){
            UserCollection userCollection = optionalUserCollection.get();
            userCollection.setBookMark(false);
            updateUserCollection(userCollection);
            return;
        }
        throw new RuntimeException("userCollection not found id is "+userCollectionId);
    }


    public List<PostResponseDto> getPostListByUserId(User authuser){
        return postRepo.findByUserId(authuser.getId()).stream().map(post -> entityToPostResponseDto(post,authuser)).toList();
    }

    public void likePost(String postId,String authId){
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("post not found"));
        UserDto userDto = userService.getUserDtoById(authId);
        User user = userService.getUserById(post.getUserId());
        Optional<Like> optionalLike = likeService.isExist(postId, authId);
        if(optionalLike.isPresent()){
            Like like = optionalLike.get();
            if(like.isLiked()){
                like.setLiked(false);
                likeService.update(like);
            }else{
                like.setLiked(true);
                likeService.update(like);
                boolean isNotificationOn = userPreferenceService.isOnLikeCommentNotification(post.getUserId());
                if(isNotificationOn) {
                    NotificationDto notificationDto = new InteractionNotificationDto(UUID.randomUUID().toString(), userDto, user.getEmail(), "like on your post", NotificationType.POST, post.getPostURL());
                    notificationService.sendNotification(notificationDto);
                }
            }
            return;
        }
        Like like = Like.builder()
                .liked(true)
                .userId(authId)
                .entityId(postId)
                .entityType(EntityType.POST)
                .build();
        likeService.save(like);
        boolean isNotificationOn = userPreferenceService.isOnLikeCommentNotification(post.getUserId());
        if(isNotificationOn) {
            NotificationDto notificationDto = new InteractionNotificationDto(UUID.randomUUID().toString(), userDto, user.getEmail(), "like on your post", NotificationType.POST, post.getPostURL());
            notificationService.sendNotification(notificationDto);
        }
    }

    public Page<PostResponseDto> loadPost(Integer pageNum, String token) {
        // Get the authenticated user
        User authUser = userService.getAuthUser(token);

        // Define pagination and sorting
        Pageable pageRequest = PageRequest.of(pageNum, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.DESC, "createdAt");

        // Step 1: Get the list of followed user IDs
        Query connectionQuery = new Query(Criteria.where("followerId").is(authUser.getId()));
        List<String> followedUserIds = new ArrayList<>(mongoTemplate.find(connectionQuery, UserConnection.class)
                .stream()
                .map(UserConnection::getUserId)
                .toList());

        // Add the authenticated user's ID to the list of followed users
        followedUserIds.add(authUser.getId());

        // Step 2: Build criteria for public posts or posts by followed users (including self)
        Criteria publicPosts = Criteria.where("privatePost").is(false);
        Criteria privatePosts = Criteria.where("userId").in(followedUserIds);

        // Combine criteria
        Query postQuery = new Query(new Criteria().orOperator(publicPosts, privatePosts))
                .with(pageRequest);

        // Step 3: Fetch posts
        List<Post> posts = mongoTemplate.find(postQuery, Post.class);
        List<PostResponseDto> postResponseDtoList = posts.stream()
                .map(post -> this.entityToPostResponseDto(post, authUser))
                .toList();

        // Step 4: Count total posts matching the query
        long total = mongoTemplate.count(postQuery.skip(-1).limit(-1), Post.class);

        return new PageImpl<>(postResponseDtoList, pageRequest, total);
    }


    public Page<PostResponseDto> loadUserPost(String userId, String authToken, Integer pageNum){
        System.out.println("page number  for load user post "+pageNum);
        User user = userService.getUserById(userId);
        User authUser = userService.getAuthUser(authToken);
        Pageable pageRequest=PageRequest.of(pageNum,DEFAULT_CAPACITY_FOR_PAGE_SIZE,Sort.Direction.DESC,"createdAt");
        Page<Post> postPage = postRepo.findByUserId(user.getId(), pageRequest);
        List<PostResponseDto> postModelList = postPage.stream().map(post->entityToPostResponseDto(post,authUser)).collect(Collectors.toList());
        return new PageImpl<>(postModelList,pageRequest,postPage.getTotalElements());
    }

    public Post getPostById(String postId){
        return postRepo.findById(postId).orElseThrow(()->new RuntimeException("provided "+postId+" post not found"));
    }

    private PostResponseDto entityToPostResponseDto(Post post,User authUser){
        UserDto userDto = userService.getUserDtoById(post.getUserId());
        return PostResponseDto.builder()
                .postId(post.getId())
                .postUser(userDto)
                .postURL(post.getPostURL())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likeCount((int) likeService.countLikeEntityByEntityIdAndEntityType(post.getId(),EntityType.POST))
                .isLike(likeService.isLike(post.getId(),authUser.getId()))
                .isBookMark(isBookMarked(post.getId(),authUser.getId()))
                .build();
    }

    public void bookMarkPost(String postId,User authUser){
        Optional<UserCollection> optionalUserCollection = userCollectionRepo.findByEntityIdAndUserId(postId, authUser.getId());
        if(optionalUserCollection.isPresent()){
            UserCollection userCollection = optionalUserCollection.get();
            userCollection.setBookMark(!userCollection.isBookMark());
            updateUserCollection(userCollection);
            return;
        }
        UserCollection userCollection = UserCollection.builder()
                .bookMark(true)
                .userId(authUser.getId())
                .entityId(postId)
                .entityType(EntityType.POST)
                .build();
        saveUserCollection(userCollection);
    }

    public void updateUserCollection(UserCollection userCollection){
        userCollectionRepo.save(userCollection);
    }

    public void saveUserCollection(UserCollection userCollection){
        userCollectionRepo.save(userCollection);
    }


    public Page<CommentResponseDto> getPostComment(String postId,Integer pageNum){
       return commentService.getCommentByEntityId(postId,pageNum);
    }

    public List<CommentResponseDto> getPostCommentList(String postId){
        return commentService.getCommentListByEntityId(postId);
    }
    public List<UserCollectionDto> getBookMarkPost(String authToken){
        User authUser = userService.getAuthUser(authToken);
        List<UserCollection> userCollections = userCollectionRepo.findByUserIdAndBookMarkAndEntityType(authUser.getId(),true,EntityType.POST).orElse(new ArrayList<>());
        if(!userCollections.isEmpty()){
            return userCollections.stream().map(this::userCollectionDto).toList();
        }
        return Collections.emptyList();
    }

    public void comment(CommentRequestDto commentRequestDto, String token){
        User authUser = userService.getAuthUser(token);
        String entityId = commentRequestDto.getEntityId();
        Post post = getPostById(entityId);
        User user = userService.getUserById(post.getUserId());
        commentService.saveComment(commentRequestDto,authUser,user);
    }
    public Integer getUserPostCount(String authId){
        List<Post> postList = postRepo.findByUserId(authId);
        return  postList.size();
    }

    public void deleteAllPostByUserId(String id){
        List<Post> postList = postRepo.findByUserId(id);
        postList.stream().parallel().filter(post -> userCollectionRepo.existsByEntityId(post.getId())).forEach(post -> {
            UserCollection userCollection=userCollectionRepo.findByEntityId(post.getId());
            userCollection.setBookMark(false);
            userCollectionRepo.save(userCollection);
        });
        postList.forEach(post -> likeService.deleteByEntityId(post.getId()));
        postList.forEach(post -> commentService.deletePostComment(post.getId()));
        postRepo.deleteByUserId(id);
    }

    public boolean isBookMarked(String entityId,String userId){
        return userCollectionRepo.findByEntityIdAndUserIdAndBookMark(entityId,userId,true).isPresent();
    }

    public UserCollectionDto userCollectionDto(UserCollection userCollection){
        String entityId = userCollection.getEntityId();
        Post post = getPostById(entityId);
        return UserCollectionDto.builder().id(userCollection.getId())
                .id(userCollection.getId())
                .entityId(userCollection.getEntityId())
                .savedAt(userCollection.getCreatedAt())
                .caption(userCollection.getEntityCaption())
                .entityType(EntityType.POST)
                .entityURL(post.getPostURL())
                .build();
    }

    public PostResponseDto getPostResponseDtoByPostId(String postId,String authToken) {
        User authUser = userService.getAuthUser(authToken);
        Post post = getPostById(postId);
        return entityToPostResponseDto(post,authUser);
    }

    public long getTotalPostCount() {
        return postRepo.count();
    }

    public List<LikeResponseDto> getLikeUserList(String postId,String token){
        return likeService.getLikeListByEntityId(postId);
    }
}
