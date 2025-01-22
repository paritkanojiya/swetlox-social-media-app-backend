package com.swetlox_app.swetlox.dto.notification;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;


@Getter
public class InteractionNotificationDto extends NotificationDto{
    private final String entityURL;
    public InteractionNotificationDto(String id, UserDto sender, String receiverEmail, String message, NotificationType notificationType,String entityURL) {
        super(id, sender, receiverEmail, message,notificationType);
        this.entityURL=entityURL;
    }
}
