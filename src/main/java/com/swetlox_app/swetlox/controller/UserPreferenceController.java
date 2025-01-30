package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.UserPreferenceService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/setting")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;
    private final UserService userService;


    @PutMapping("/private-account/{newValue}")
    public void changePrivateAccountSetting(@PathVariable("newValue") boolean newValue,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        System.out.println("new value"+newValue);
        userPreferenceService.changePrivateAccountSetting(authUser.getId(),newValue);
    }

    @PutMapping("/auto-follower/{newValue}")
    public void changeAutoFollowSetting(@PathVariable("newValue") boolean newValue,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userPreferenceService.changeAutoFollowSetting(authUser.getId(),newValue);
    }

    @PutMapping("/like-comment-notification/{newValue}")
    public void changeLikeCommentNotificationSetting(@PathVariable("newValue") boolean newValue,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userPreferenceService.changeLikeCommentNotificationSetting(authUser.getId(),newValue);
    }

    @PutMapping("/follow-notification/{newValue}")
    public void changeNewFollowerNotification(@PathVariable("newValue") boolean newValue,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        userPreferenceService.changeNewFollowerNotification(authUser.getId(),newValue);
    }
}
