package com.swetlox_app.swetlox.dto.notification;

import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;


public class ConnectionRequestNotificationDto extends NotificationDto {
    public ConnectionRequestNotificationDto(String id, UserDto sender, String receiverEmail, String message, NotificationType notificationType) {
        super(id, sender, receiverEmail, message,notificationType);
    }
}
