package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.dto.message.ImageMedia;
import com.swetlox_app.swetlox.dto.message.MessageSendDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TestConroller {



//    @GetMapping("/test")
//    public ResponseEntity<MessageSendDto> test(){
//        MessageSendDto build = MessageSendDto.builder()
//                .media(new ImageMedia("imageURL", "this is nice image"))
//                .isRead(false)
//                .mediaType(MediaType.IMAGE)
//                .recipientId("1")
//                .senderId("2")
//                .timeStamp(LocalDateTime.now())
//                .build();
//        return ResponseEntity.ok(build);
//    }
}
