package com.swetlox_app.swetlox.dto.message;

import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageSendDto {
    private String messageId;
    private MediaType mediaType;
    private String senderId;
    private String recipientId;
    private Media media;
    private LocalDateTime timeStamp;
    private MessageStatus status;
}
