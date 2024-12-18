package com.swetlox_app.swetlox.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostModel {
    private String postId;
    private String userId;
    private String userName;
    private String profileURL;
    private String caption;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private String postURL;
    private boolean isLike;
    private boolean isBookMark;
}
