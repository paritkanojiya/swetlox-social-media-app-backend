package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.entity.Comment;
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

@RestController
@RequestMapping("/v1/api/reel")
@RequiredArgsConstructor
public class ReelController {

    private final UserService userService;
    private final CommentService commentService;
    private final ReelsService reelsService;

    @PostMapping
    public ResponseEntity<ReelsModel> createReel(@RequestHeader("Authorization") String token, @RequestParam("caption") String caption, @RequestPart("file") MultipartFile file) throws IOException {
        User authUser = userService.getAuthUser(token);
        ReelsModel reelsModel = reelsService.saveReel(authUser, file, caption);
        return ResponseEntity.status(HttpStatus.CREATED).body(reelsModel);
    }

    @GetMapping("/{reelId}")
    public ResponseEntity<ReelsModel> getReel(@PathVariable("reelId") String reelId){
        ReelsModel reelsModel = reelsService.getReelById(reelId);
        return ResponseEntity.ok(reelsModel);
    }

    @DeleteMapping("/{reelId}")
    public ResponseEntity<String> deleteReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        reelsService.deleteReels(reelId,authUser.getId());
        return ResponseEntity.ok("reel deleted");
    }

    @GetMapping("/load-reels/{pageNum}")
    public ResponseEntity<Page<ReelsModel>> loadInitialReels(@PathVariable("pageNum") Integer pageNum){
        Page<ReelsModel> reelsModels = reelsService.loadReels(pageNum);
        return ResponseEntity.ok(reelsModels);
    }

    @GetMapping("/like/{reelId}")
    public ResponseEntity<?> likeReel(@PathVariable("reelId") String reelId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        reelsService.likedReels(reelId,authUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment")
    public ResponseEntity<Void> comment(@RequestBody CommentRequestDto comment, @RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        commentService.saveComment(comment, authUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") String commentId,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        commentService.deleteComment(commentId,authUser.getId());
        return ResponseEntity.ok().build();
    }
}
