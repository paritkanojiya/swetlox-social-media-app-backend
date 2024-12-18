package com.swetlox_app.swetlox.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

public class SendConnectionRequestEvent extends ApplicationEvent {
    private String authUserId;
    private String userName;
    private String requestedUserId;

    public SendConnectionRequestEvent(Object source, String authUserId, String userName, String requestedUserId) {
        super(source);
        this.authUserId = authUserId;
        this.userName = userName;
        this.requestedUserId = requestedUserId;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRequestedUserId() {
        return requestedUserId;
    }

    public void setRequestedUserId(String requestedUserId) {
        this.requestedUserId = requestedUserId;
    }
}
