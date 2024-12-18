package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.entity.ChatRoom;
import com.swetlox_app.swetlox.entity.Message;
import com.swetlox_app.swetlox.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepo messageRepo;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveMessage(String authId,String recipientId,String content){
        String chatId1= String.format("%s_%s",authId,recipientId);
        String chatId2= String.format("%s_%s",recipientId,authId);
        Optional<ChatRoom> chatRoom = chatRoomService.findChatRoom(chatId1, chatId2);
        if(chatRoom.isPresent()){
            Message message = Message.builder()
                    .sender(authId)
                    .recipient(recipientId)
                    .chatRoomId(chatRoom.get().getChatId())
                    .content(content).build();
            Message createdMessage = messageRepo.save(message);
            messagingTemplate.convertAndSend("/user/chat/message/"+recipientId,createdMessage);
        }
    }

    public List<Message> loadMessage(String authId,String recipientId){
        String chatId1= String.format("%s_%s",authId,recipientId);
        String chatId2= String.format("%s_%s",recipientId,authId);
        Optional<ChatRoom> chatRoom = chatRoomService.findChatRoom(chatId1, chatId2);
        if(chatRoom.isPresent()){
            return messageRepo.findByChatRoomId(chatRoom.get().getChatId());
        }
        return Collections.emptyList();
    }
}
