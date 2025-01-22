package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.MessageDto;
import com.swetlox_app.swetlox.dto.message.MessageRequestDTO;
import com.swetlox_app.swetlox.dto.message.MessageSendDto;
import com.swetlox_app.swetlox.entity.Message;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.MessageService;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/message")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @PostMapping("/send-message")
    public void sendMessage(@ModelAttribute MessageRequestDTO messageRequestDTO){
        System.out.println("message "+messageRequestDTO.getSender()+" "+messageRequestDTO.getRecipient()+" "+messageRequestDTO.getContent());
        try {
            messageService.sendMessage(messageRequestDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/load-message/{recipientId}")
    public ResponseEntity<List<MessageSendDto>> loadMessage(@PathVariable("recipientId") String recipientId, @RequestHeader("Authorization") String tpken){
        User authUser = userService.getAuthUser(tpken);
        List<MessageSendDto> messageSendDtoList = messageService.loadMessage(authUser.getId(), recipientId);
        return ResponseEntity.ok(messageSendDtoList);
    }


}
