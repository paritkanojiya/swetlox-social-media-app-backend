package com.swetlox_app.swetlox.util;

import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Helper {

    private final SimpMessagingTemplate simpMessagingTemplate;


    @EventListener
    public void sendNotification(SendNotificationEvent sendNotificationEvent){
        System.out.println("send "+sendNotificationEvent);
        NotificationType notificationType = sendNotificationEvent.getNotificationDto().getNotificationType();
        switch (notificationType){
            case REEL,POST,STORY -> simpMessagingTemplate.convertAndSend("/topic/notifications/"+sendNotificationEvent.getNotificationDto().getReceiverEmail(),sendNotificationEvent.getNotificationDto());
            case CONNECTION_REQUEST ->  simpMessagingTemplate.convertAndSend("/topic/following-request/"+sendNotificationEvent.getNotificationDto().getReceiverEmail(),sendNotificationEvent.getNotificationDto());
            default -> throw new RuntimeException(notificationType+" not supported");
        }
    }

}
