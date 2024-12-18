package com.swetlox_app.swetlox.util;

import com.swetlox_app.swetlox.dto.UserConnectionDto;
import com.swetlox_app.swetlox.event.SendConnectionRequestEvent;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class Helper {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    public void sendConnectionRequest(SendConnectionRequestEvent sendConnectionRequestEvent){
        UserConnectionDto userConnectionDto = UserConnectionDto.builder()
                .userId(sendConnectionRequestEvent.getAuthUserId())
                .userName(sendConnectionRequestEvent.getUserName())
                .build();
//        simpMessagingTemplate.convertAndSend("/topic/following-request/"+sendConnectionRequestEvent.getRequestedUserId(),userConnectionDto);
    }

    @EventListener
    public void sendNotification(SendNotificationEvent sendNotificationEvent){
        System.out.println("userName"+ sendNotificationEvent.getSender()+" recipient "+sendNotificationEvent.getRecipientEmail());
        simpMessagingTemplate.convertAndSend("/topic/notifications/"+sendNotificationEvent.getRecipientEmail(),
                Map.of("sender",sendNotificationEvent.getSender(),"message",sendNotificationEvent.getMessage(),"postURL",sendNotificationEvent.getPostURL()));
        System.out.println("notification send......");
    }

}
