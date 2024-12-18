package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.MessageDto;
import com.swetlox_app.swetlox.entity.Message;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.MessageService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/message")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    @PostMapping("/send-message")
    public void sendMessage(@ModelAttribute MessageDto messageDto){
        System.out.println(messageDto.getSender()+" "+messageDto.getRecipient()+" "+messageDto.getContent());
        messageService.saveMessage(messageDto.getSender(),messageDto.getRecipient(),messageDto.getContent());
    }

    @GetMapping("/load-message/{recipientId}")
    public ResponseEntity<List<Message>> loadMessage(@PathVariable("recipientId") String recipientId,@RequestHeader("Authorization") String tpken){
        User authUser = userService.getAuthUser(tpken);
        List<Message> messageList = messageService.loadMessage(authUser.getEmail(), recipientId);
        return ResponseEntity.ok(messageList);
    }
}
