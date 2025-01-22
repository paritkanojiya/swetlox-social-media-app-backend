package com.swetlox_app.swetlox.service;

import com.mongodb.client.result.UpdateResult;
import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.notification.InteractionNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
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
            commentService.deletePostComment(postGET.getId());
        }

    }

    public void removePostFromUserCollection(String userId, String postId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("postList").is(postId));
        Update update = new Update();
        update.pull("postList", postId);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, UserCollection.class);
        System.out.println(updateResult);
    }


    public List<PostResponseDto> getPostListByUserId(User authuser){
        return postRepo.findByUserId(authuser.getId()).stream().map(post -> entityToPostResponseDto(post,authuser)).toList();
    }

    public void likePost(String postId,String authId){
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("post not found"));
        boolean isLiked=postRepo.existsByIdAndLikedUserListContaining(postId,authId);
        if(isLiked){
            removeUserIdFromLikedUserList(postId,authId);
            return;
        }
        addUserIdFromLikedUserList(postId,authId);
        UserDto userDto = userService.getUserDtoById(authId);
        User user = userService.getUserById(post.getUserId());
        NotificationDto notificationDto=new InteractionNotificationDto(postId,userDto,user.getEmail(),"like on your post", NotificationType.POST,post.getPostURL());
        notificationService.sendNotification(notificationDto);
    }

    public Page<PostResponseDto> loadPost(Integer pageNum, String token){
        User authUser = userService.getAuthUser(token);
        Pageable pageRequest=PageRequest.of(pageNum,DEFAULT_CAPACITY_FOR_PAGE_SIZE,Sort.Direction.DESC,"createdAt");
        Query connectionQuery = new Query(Criteria.where("followerId").is(authUser.getId()));
        List<String> followedUserIds = mongoTemplate.find(connectionQuery, UserConnection.class)
                .stream()
                .map(UserConnection::getUserId)
                .toList();

        // Step 2: Build criteria for public or follower-visible posts
        Criteria publicPosts = Criteria.where("privatePost").is(false);
        Criteria privatePosts = Criteria.where("userId").in(followedUserIds);

        Query postQuery = new Query(new Criteria().orOperator(publicPosts, privatePosts))
                .with(pageRequest);

        // Step 3: Fetch posts
        List<Post> posts = mongoTemplate.find(postQuery, Post.class);
        List<PostResponseDto> postResponseDtoList = posts.stream().map(post -> this.entityToPostResponseDto(post, authUser)).toList();
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

    private void removeUserIdFromLikedUserList(String postId, String userId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().pull("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    private void addUserIdFromLikedUserList(String postId, String userId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().addToSet("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }
    private PostResponseDto entityToPostResponseDto(Post post,User authUser){
        UserDto userDto = userService.getUserDtoById(post.getUserId());
        return PostResponseDto.builder()
                .postId(post.getId())
                .postUser(userDto)
                .postURL(post.getPostURL())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .likeCount(post.getLikedUserList() != null ? post.getLikedUserList().size() : 0)
                .isLike(postRepo.existsByIdAndLikedUserListContaining(post.getId(), authUser.getId()))
                .isBookMark(userCollectionRepo.existsByUserIdAndPostListContains(authUser.getId(), post.getId()))
                .build();
    }

    public void bookMarkPost(String postId,User authUser){
      UserCollection  userCollection=userCollectionRepo.findByUserId(authUser.getId()).orElse(new UserCollection());
      boolean contains = userCollection.getPostList().contains(postId);
      if(userCollection.getUserId()==null)
          userCollection.setUserId(authUser.getId());
      if(contains)
          userCollection.getPostList().remove(postId);
      else
          userCollection.getPostList().add(postId);
      userCollectionRepo.save(userCollection);
    }

    public UserCollection newUserCollections(String authId){
        UserCollection userCollection=new UserCollection();
        userCollection.setUserId(authId);
        return userCollection;
    }

    public List<Comment> getPostComment(String postId){
       return commentService.getPostComment(postId);
    }
    
    public List<PostResponseDto> getBookMarkPost(String authToken){
        User authUser = userService.getAuthUser(authToken);
        Optional<UserCollection> optionalUserCollection = userCollectionRepo.findByUserId(authUser.getId());
        if(optionalUserCollection.isPresent()){
            UserCollection userCollection = optionalUserCollection.get();
           return userCollection.getPostList().stream().map(this::getPostById).map(post -> entityToPostResponseDto(post,authUser)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Integer getUserPostCount(String authId){
        List<Post> postList = postRepo.findByUserId(authId);
        return  postList.size();
    }

    public void deleteAllPostByUserId(String id){
        postRepo.deleteByUserId(id);
    }

    public PostResponseDto getPostResponseDtoByPostId(String postId,String authToken) {
        User authUser = userService.getAuthUser(authToken);
        Post post = getPostById(postId);
        return entityToPostResponseDto(post,authUser);
    }
}
