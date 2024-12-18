package com.swetlox_app.swetlox.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class SendNotificationEvent extends ApplicationEvent {

    private final String message;
    private final String postURL;
    private final String sender;
    private final String recipientEmail;

    public SendNotificationEvent(Object source, String message, String postURL, String sender, String recipientEmail) {
        super(source);
        this.message = message;
        this.postURL = postURL;
        this.sender = sender;
        this.recipientEmail = recipientEmail;

    }
}
