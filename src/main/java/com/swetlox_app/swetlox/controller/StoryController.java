package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.StoryModel;
import com.swetlox_app.swetlox.service.StoryService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/save")
    public void saveStoryApi(@RequestParam("multipartFiles") List<MultipartFile> multipartFileList, @RequestHeader("Authorization") String authToken){

        User authUser = userService.getAuthUser(authToken);
        storyService.createStory(multipartFileList,authUser.getId());
    }

    @GetMapping("/get-connection-story")
    public ResponseEntity<List<StoryModel>> getConnectionStory(@RequestHeader("Authorization") String authToken){
        User authUser = userService.getAuthUser(authToken);
        List<StoryModel> connectionStory = storyService.getConnectionStory(authUser.getId());
        connectionStory.forEach(storyModel -> System.out.println(storyModel.getProfileURL()));
        return ResponseEntity.ok(connectionStory);
    }   
}
