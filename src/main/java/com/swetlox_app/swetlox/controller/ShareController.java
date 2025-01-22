package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.post.PostResponseDto;
import com.swetlox_app.swetlox.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/share")
public class ShareController {

    private final PostService postService;

//    @GetMapping("/post/{postId}")
//    public ResponseEntity<PostResponseDto> getPost(@PathVariable("postId") String postId){
////        PostResponseDto postModel = postService.getPostResponseDtoByPostId(postId,);
////        return ResponseEntity.ok(postModel);
//    }
}
