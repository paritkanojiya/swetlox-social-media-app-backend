package com.swetlox_app.swetlox.service;

import com.cloudinary.Cloudinary;
import com.mongodb.client.result.UpdateResult;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import com.swetlox_app.swetlox.model.PostModel;
import com.swetlox_app.swetlox.repository.PostRepo;
import com.swetlox_app.swetlox.repository.UserCollectionRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
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
    private final Cloudinary cloudinaryTemplate;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
    private final UserCollectionRepo userCollectionRepo;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentService commentService;
    

    public PostModel savePost(User authUser, MultipartFile file,String caption) throws IOException {
        Map uploaded = cloudinaryTemplate.uploader().upload(file.getBytes(), Collections.emptyMap());
        String postURL=(String)uploaded.get("url");
        System.out.println(postURL);
        Post createdPost = Post.builder().userId(authUser.getId())
                .caption(caption)
                .createdAt(LocalDateTime.now())
                .postURL(postURL)
                .build();
        Post savedPost = postRepo.save(createdPost);
        return modelMapper.map(savedPost, PostModel.class);
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
    public List<PostModel> getPostListByUserId(User authuser){
        List<PostModel> postModelList=new ArrayList<>();
        postRepo.findByUserId(authuser.getId()).forEach(post-> {
            PostModel postModel = modelMapper.map(post, PostModel.class);
            User user = userService.getUserById(authuser.getId());
            postModel.setProfileURL(user.getProfileURL());
            postModel.setUserName(user.getUserName());
            List<String> likedUserList = post.getLikedUserList();
            postModel.setLikeCount(likedUserList!=null ? likedUserList.size() : 0);
            postModelList.add(postModel);
        });
        return postModelList;
    }

    public void likePost(String postId,String authId){
        User authUser = userService.getUserById(authId);
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("post not found"));
        User postUser = userService.getUserById(post.getUserId());
        boolean isLiked=postRepo.existsByIdAndLikedUserListContaining(postId,authId);
        if(isLiked){
            removeUserIdFromLikedUserList(postId,authId);
            return;
        }
        addUserIdFromLikedUserList(postId,authId);
        SendNotificationEvent sendNotificationEvent = new SendNotificationEvent(this,"like your post",post.getPostURL() , authUser.getUserName(),postUser.getEmail());
        eventPublisher.publishEvent(sendNotificationEvent);
    }
    public Page<PostModel> loadPost(Integer pageNum,String token){
        User authUser = userService.getAuthUser(token);
        Pageable pageRequest= PageRequest.of(pageNum,10, Sort.by(Sort.Direction.DESC,"createdAt"));
        Page<Post> postPage = postRepo.findAll(pageRequest);
        List<PostModel> postModelList = postPage.map(this::convertPostModel).map(postModel -> {
            User user = userService.getUserById(postModel.getUserId());
            postModel.setUserName(user.getUserName());
            postModel.setProfileURL(user.getProfileURL());
            postModel.setLike(postRepo.existsByIdAndLikedUserListContaining(postModel.getPostId(),authUser.getId()));
            postModel.setBookMark(userCollectionRepo.existsByUserIdAndPostListContains(authUser.getId(),postModel.getPostId()));
            return postModel;
        }).stream().collect(Collectors.toList());
        return new PageImpl<>(postModelList,pageRequest,postPage.getTotalElements());
    }

    public PostModel getPostByPostId(String postId){
        return postRepo.findById(postId).map(this::convertPostModel).orElse(null);
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
    private PostModel convertPostModel(Post post){
        PostModel postModel = modelMapper.map(post, PostModel.class);
        postModel.setLikeCount(post.getLikedUserList()!=null ? post.getLikedUserList().size() : 0);
        return postModel;
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
    
    public List<PostModel> getBookMarkPost(String authId){
        Optional<UserCollection> optionalUserCollection = userCollectionRepo.findByUserId(authId);
        if(optionalUserCollection.isPresent()){
            UserCollection userCollection = optionalUserCollection.get();
           return userCollection.getPostList().stream().map(this::getPostByPostId).collect(Collectors.toList());
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
}
