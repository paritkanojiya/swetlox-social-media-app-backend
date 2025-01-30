package com.swetlox_app.swetlox.dto.admin;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StatusDto {
    private Integer activeUser;
    private Integer totalUser;
    private Integer totalPost;
    private Integer pastWeekUser;
    private Integer reportedPost;
}
