package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.entity.ChatRoom;
import com.swetlox_app.swetlox.entity.RecentChatHistory;
import com.swetlox_app.swetlox.repository.ChatRoomRepo;
import com.swetlox_app.swetlox.repository.RecentChatHistoryRepo;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.StringUtil;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepo chatRoomRepo;
    private final RecentChatHistoryRepo chatHistoryRepo;
    private final UserService userService;

    public void createChatRoom(String authUser,String requestedUser){
        boolean isUserExist = userService.isUserExistById(requestedUser);
        if(!isUserExist) throw new RuntimeException("provided id user not found");
        String chatId1= String.format("%s_%s",authUser,requestedUser);
        String chatId2= String.format("%s_%s",requestedUser,authUser);
        boolean chatRoomExists=findChatId(chatId1,chatId2);
        if (!chatRoomExists) {
            ChatRoom chatRoom = ChatRoom
                    .builder().chatId(chatId1)
                    .build();
            chatRoomRepo.save(chatRoom);
            RecentChatHistory chatHistory = recentChatHistoryBuilder(requestedUser,authUser);
            saveRecentChatHistory(chatHistory);
        }
        Optional<RecentChatHistory> optionalRecentChatHistory = chatHistoryRepo.findByUserIdAndAuthUserId(requestedUser, authUser);
        if(optionalRecentChatHistory.isPresent()){
            RecentChatHistory recentChatHistory = optionalRecentChatHistory.get();
            updateLastInteractionTime(recentChatHistory);
        }
    }

    public List<RecentChatHistory> getRecentChatHistory(String authUserId){
        return chatHistoryRepo.findByAuthUserId(authUserId,Sort.by(Sort.Direction.DESC,"lastInteraction"));
    }

    private void updateLastInteractionTime(RecentChatHistory recentChatHistory){
        recentChatHistory.setLastInteraction(LocalDateTime.now());
        chatHistoryRepo.save(recentChatHistory);
    }

    private RecentChatHistory recentChatHistoryBuilder(String requestedUser,String authUser){
        return RecentChatHistory.builder()
                .userId(requestedUser)
                .authUserId(authUser)
                .lastInteraction(LocalDateTime.now())
                .build();
    }

    private void saveRecentChatHistory(RecentChatHistory chatHistory){
        chatHistoryRepo.save(chatHistory);
    }

    public boolean findChatId(String chatId1,String chatId2){
       return chatRoomRepo.existsByChatIdIn(List.of(chatId1, chatId2));
    }

    public Optional<ChatRoom> findChatRoom(String chatId1,String chatId2){
        return chatRoomRepo.findByChatIdIn(List.of(chatId1,chatId2));
    }

    public String getChatId(String authId,String recipientId){
        return String.format("%s_%s",authId,recipientId);
    }
}
