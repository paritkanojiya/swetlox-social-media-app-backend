package com.swetlox_app.swetlox.event;


import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@Getter
@ToString
public class SendNotificationEvent extends ApplicationEvent {

    private final NotificationDto notificationDto;

    public SendNotificationEvent(Object source,NotificationDto notificationDto) {
        super(source);
        this.notificationDto=notificationDto;
    }


}
