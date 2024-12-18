package com.swetlox_app.swetlox.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class StoryModel {
    private String fullName;
    private String userName;
    private String profileURL;
    private List<String> storyList;
}
