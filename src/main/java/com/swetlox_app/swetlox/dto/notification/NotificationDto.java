package com.swetlox_app.swetlox.dto.notification;


import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public abstract class NotificationDto {
    private String id;
    private UserDto sender;
    private String receiverEmail;
    private String message;
    private NotificationType notificationType;
}
