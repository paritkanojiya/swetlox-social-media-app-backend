package com.swetlox_app.swetlox.dto.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponseDto {
    private String userId;
    private Integer totalPost;
    private Integer totalFollower;
    private Integer totalFollowing;
    private Integer totalLike;
    private Integer totalComment;
    private Integer totalBookMark;
}
