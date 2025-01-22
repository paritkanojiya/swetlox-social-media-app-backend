package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.story.StoryRequestDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.dto.story.StoryResponseDto;
import com.swetlox_app.swetlox.service.CommentService;
import com.swetlox_app.swetlox.service.StoryService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/story")
public class StoryController {

    private final StoryService storyService;
    private final UserService userService;
    private final CommentService commentService;

    @PostMapping("/save")
    public void saveStory(@RequestParam("files") List<MultipartFile> files, @RequestParam("mediaTypes") List<MediaType> mediaTypes, @RequestHeader("Authorization") String authToken){
        User authUser = userService.getAuthUser(authToken);
        storyService.createStory(files,mediaTypes,authUser.getId());
    }

    @GetMapping("/get-connection-story")
    public ResponseEntity<?> getConnectionStory(@RequestParam(value = "self",defaultValue = "false") boolean isSelf,@RequestHeader("Authorization") String authToken){
        User authUser = userService.getAuthUser(authToken);
        if(isSelf){
            StoryResponseDto selfStory = storyService.getSelfStory(authUser);
            return ResponseEntity.ok(selfStory);
        }
        List<StoryResponseDto> connectionStory = storyService.getConnectionStory(authUser);
        return ResponseEntity.ok(connectionStory);
    }

    @PostMapping("/comment")
    public void doComment(@RequestBody CommentRequestDto commentRequestDto,@RequestHeader("Authorization") String authToken){
        User authUser = userService.getAuthUser(authToken);
        commentService.saveComment(commentRequestDto,authUser);
    }

    @PostMapping("/like/{storyId}")
    public ResponseEntity<?> likeStory(@PathVariable("storyId") String storyId,@RequestHeader("Authorization") String token){
        storyService.likeStory(storyId,token);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/like/{storyId}")
    public ResponseEntity<Page<LikeResponseDto>> getLikeInfo(@PathVariable("storyId") String storyId, @RequestParam("pageNum") Integer pageNum, @RequestHeader("Authorization") String token){
        Page<LikeResponseDto> likeResponseDtoPage = storyService.getLikeResponseDtoByEntityId(storyId, pageNum);
        return ResponseEntity.ok(likeResponseDtoPage);
    }

    @GetMapping("/comment/{entityId}")
    public ResponseEntity<Page<CommentResponseDto>> getComment(@PathVariable("entityId") String entityId,@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum){
        Page<CommentResponseDto> commentResponseDtoList = commentService.getCommentByEntityId(entityId, pageNum);
        return ResponseEntity.ok(commentResponseDtoList);
    }
}
