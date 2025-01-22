package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher eventPublisher;

    public void sendNotification(NotificationDto notificationDto){
        SendNotificationEvent sendNotificationEvent=new SendNotificationEvent(this,notificationDto);
        eventPublisher.publishEvent(sendNotificationEvent);
    }
}
