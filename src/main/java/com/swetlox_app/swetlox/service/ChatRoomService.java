package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.entity.ChatRoom;
import com.swetlox_app.swetlox.repository.ChatRoomRepo;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepo chatRoomRepo;

    public void createChatRoom(String authUser,String requestedUser){
        String chatId1= String.format("%s_%s",authUser,requestedUser);
        String chatId2= String.format("%s_%s",requestedUser,authUser);
        boolean chatRoomExists=findChatId(chatId1,chatId2);
        if (!chatRoomExists) {
            ChatRoom chatRoom = ChatRoom
                    .builder().chatId(chatId1)
                    .build();
            chatRoomRepo.save(chatRoom);
        }
    }

    public boolean findChatId(String chatId1,String chatId2){
       return chatRoomRepo.existsByChatIdIn(List.of(chatId1, chatId2));
    }

    public Optional<ChatRoom> findChatRoom(String chatId1,String chatId2){
        return chatRoomRepo.findByChatIdIn(List.of(chatId1,chatId2));
    }
}
