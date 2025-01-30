package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.reel.ReelResponseDto;
import com.swetlox_app.swetlox.dto.usercollection.UserCollectionDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.ReelsModel;
import com.swetlox_app.swetlox.service.CommentService;
import com.swetlox_app.swetlox.service.ReelsService;
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
@RequestMapping("/v1/api/reel")
@RequiredArgsConstructor
public class ReelController {

    private final UserService userService;
    private final CommentService commentService;
    private final ReelsService reelsService;

    @PostMapping
    public ResponseEntity<Void> createReel(@RequestHeader("Authorization") String token, @RequestParam("caption") String caption, @RequestPart("file") MultipartFile file) throws IOException {
        User authUser = userService.getAuthUser(token);
        reelsService.saveReel(authUser, file, caption);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{reelId}")
    public ResponseEntity<ReelResponseDto> getReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        ReelResponseDto reelResponseById = reelsService.getReelResponseById(reelId, token);
        return ResponseEntity.ok(reelResponseById);
    }

    @DeleteMapping("/{reelId}")
    public ResponseEntity<String> deleteReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        reelsService.deleteReels(reelId,authUser.getId());
        return ResponseEntity.ok("reel deleted");
    }

    @GetMapping("/get-reels/{userId}")
    public ResponseEntity<Page<ReelResponseDto>> getReels(@RequestParam("pageNum") Integer pageNum,@PathVariable("userId") String userId,@RequestHeader("Authorization") String token){
        Page<ReelResponseDto> reelResponseDtoPage = reelsService.getReelByUserId(userId, pageNum, token);
        return ResponseEntity.ok(reelResponseDtoPage) ;
    }

    @GetMapping("/load-reels/{pageNum}")
    public ResponseEntity<Page<ReelResponseDto>> loadInitialReels(@PathVariable("pageNum") Integer pageNum,@RequestHeader("Authorization") String token){
        Page<ReelResponseDto> reelResponseDtos = reelsService.loadReels(pageNum, token);
        return ResponseEntity.ok(reelResponseDtos);
    }

    @GetMapping("/like/{reelId}")
    public ResponseEntity<?> likeReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        reelsService.likedReels(reelId,authUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment")
    public ResponseEntity<Void> comment(@RequestBody CommentRequestDto comment, @RequestHeader("Authorization") String token){
        reelsService.comment(comment,token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/comment/{reelId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentList(@PathVariable("reelId") String reelId){
        List<CommentResponseDto> commentList = reelsService.getCommentList(reelId);
        return ResponseEntity.ok(commentList);
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") String commentId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        commentService.deleteComment(commentId,authUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/book-mark/{reelId}")
    public void bookMarkReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        reelsService.bookMarkReel(reelId,token);
    }

    @GetMapping("/get-bookmark-reel")
    public ResponseEntity<List<UserCollectionDto>> getBookMarkReels(@RequestHeader("Authorization") String token){
        List<UserCollectionDto> bookMarkReels = reelsService.getBookMarkReels(token);
        return ResponseEntity.ok(bookMarkReels);
    }
}
