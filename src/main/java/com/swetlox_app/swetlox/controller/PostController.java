package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.usercollection.UserCollectionDto;
import com.swetlox_app.swetlox.entity.Comment;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.dto.post.PostResponseDto;
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
    public ResponseEntity<Void> createPost(@RequestHeader("Authorization") String token, @RequestParam("visibility") boolean visibility, @RequestParam("caption") String caption, @RequestPart("file")MultipartFile file) throws IOException {
        User authUser = userService.getAuthUser(token);
        postService.savePost(authUser, file, caption,visibility);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/get-post")
    public ResponseEntity<List<PostResponseDto>> getAuthUserPost(@RequestParam(value = "pageNum",defaultValue = "0") Integer pageNum, @RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        List<PostResponseDto> postModelList = postService.getPostListByUserId(authUser);
        return ResponseEntity.ok(postModelList);
    }

    @GetMapping("/get-post/{id}")
    public ResponseEntity<Page<PostResponseDto>> getUserPost(@PathVariable("id") String userId, @RequestParam(value = "pageNum",defaultValue = "0") Integer pageNum, @RequestHeader("Authorization") String authToken){
        Page<PostResponseDto> postModelPage = postService.loadUserPost(userId, authToken, pageNum);
        return ResponseEntity.ok(postModelPage);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String authToken){
        PostResponseDto postModel = postService.getPostResponseDtoByPostId(postId,authToken);
        return ResponseEntity.ok(postModel);
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.deletePost(postId,authUser.getId());
        return ResponseEntity.ok("post deleted");
    }
    
    @GetMapping("/load-posts/{pageNum}")
    public ResponseEntity<Page<PostResponseDto>> loadInitialPost(@PathVariable("pageNum") Integer pageNum, @RequestHeader("Authorization") String token){
        Page<PostResponseDto> postModels = postService.loadPost(pageNum,token);
        return ResponseEntity.ok(postModels);
    } 
    
    @GetMapping("/like/{postId}")
    public ResponseEntity<?> likePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.likePost(postId,authUser.getId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/comment")
    public ResponseEntity<Void> comment(@RequestBody CommentRequestDto commentRequestDto, @RequestHeader("Authorization") String token){
        postService.comment(commentRequestDto,token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
    
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") String commentId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        commentService.deleteComment(commentId,authUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comment/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getPostComment(@PathVariable("postId") String postId){
        List<CommentResponseDto> postCommentList = postService.getPostCommentList(postId);
        return ResponseEntity.ok(postCommentList);
    }
    

    @GetMapping("/bookmark-post/{postId}")
    public void savePost(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
           postService.bookMarkPost(postId,userService.getAuthUser(token));
    }

    @GetMapping("/bookmark-post")
    public ResponseEntity<List<UserCollectionDto>> bookmarkPost(@RequestHeader("Authorization") String token){
        List<UserCollectionDto> bookMarkPost = postService.getBookMarkPost(token);
        return ResponseEntity.ok(bookMarkPost);
    }

    @DeleteMapping("/bookmark-delete/{postId}")
    public void bookmarkPostDelete(@PathVariable("postId") String postId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        postService.removePostFromUserCollection(authUser.getId(),postId);
    }

    @GetMapping("/like-user-list/{postId}")
    public ResponseEntity<List<LikeResponseDto>> getLikeUserList(@PathVariable("postId") String postId, @RequestHeader("Authorization") String token){
        List<LikeResponseDto> likeUserList = postService.getLikeUserList(postId, token);
        return ResponseEntity.ok(likeUserList);
    }
}
