package com.swetlox_app.swetlox.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileModel {
    private String fullName;
    private String userName;
    private String profileURL;
    private Integer follower;
    private Integer following;
    private Integer postCount;
    private List<String> bio;
}