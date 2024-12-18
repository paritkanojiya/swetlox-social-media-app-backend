package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.Comment;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.PostModel;
import com.swetlox_app.swetlox.service.CommentService;
import com.swetlox_app.swetlox.service.PostService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/post")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    
    @PostMapping
    public ResponseEntity<PostModel> createPost(@RequestHeader("Authorization") String token,@RequestParam("caption") String caption, @RequestPart("file")MultipartFile file) throws IOException {
        User authUser = userService.getAuthUser(token);
        PostModel postModel = postService.savePost(authUser, file, caption);
        return ResponseEntity.status(HttpStatus.CREATED).body(postModel);
    }
    
    @GetMapping("/get-post")
    public ResponseEntity<List<PostModel>> getAuthUserPost(@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<PostModel> postModelList = postService.getPostListByUserId(authUser);
        return ResponseEntity.ok(postModelList);
    }
    @GetMapping("/{postId}")
    public ResponseEntity<PostModel> getPost(@PathVariable("postId") String postId){
        PostModel postModel = postService.getPostByPostId(postId);
        return ResponseEntity.ok(postModel);
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.deletePost(postId,authUser.getId());
        return ResponseEntity.ok("post deleted");
    }
    
    @GetMapping("/load-posts/{pageNum}")
    public ResponseEntity<Page<PostModel>> loadInitialPost(@PathVariable("pageNum") Integer pageNum,@RequestHeader("Authorization") String token){
        Page<PostModel> postModels = postService.loadPost(pageNum,token);
        return ResponseEntity.ok(postModels);
    } 
    
    @GetMapping("/like/{postId}")
    public ResponseEntity<?> likePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.likePost(postId,authUser.getId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/comment")
    public ResponseEntity<Comment> comment(@ModelAttribute Comment comment,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        Comment savedComment = commentService.saveComment(comment, authUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedComment);
    }
    
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") String commentId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        commentService.deleteComment(commentId,authUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comment/{postId}")
    public ResponseEntity<List<Comment>> getPostComment(@PathVariable("postId") String postId){
        List<Comment> postComment = postService.getPostComment(postId);
        return ResponseEntity.ok(postComment);
    }

    @GetMapping("/bookmark-post/{postId}")
    public void savePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
           postService.bookMarkPost(postId,userService.getAuthUser(token));
    }

    @GetMapping("/bookmark-post")
    public ResponseEntity<List<PostModel>> bookmarkPost(@RequestHeader("Authorization") String token){
        List<PostModel> bookMarkPost = postService.getBookMarkPost(userService.getAuthUser(token).getId());
        return ResponseEntity.ok(bookMarkPost);
    }

    @DeleteMapping("/bookmark-delete/{postId}")
    public void bookmarkPostDelete(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.removePostFromUserCollection(authUser.getId(),postId);
    }
}
