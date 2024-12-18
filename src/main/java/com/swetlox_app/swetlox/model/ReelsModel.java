package com.swetlox_app.swetlox.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReelsModel {
    private String reelId;
    private String userId;
    private String caption;
    private LocalDateTime createdAt;
    private String reelsURL;
}
