package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.ChatRoomService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/chat")
public class ChatController {
    
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    
    @GetMapping("/create-chatroom/{recipientId}")
    public void createChatRoom(@PathVariable("recipientId") String recipientId, @RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        chatRoomService.createChatRoom(authUser.getEmail(),recipientId);
    }
}
