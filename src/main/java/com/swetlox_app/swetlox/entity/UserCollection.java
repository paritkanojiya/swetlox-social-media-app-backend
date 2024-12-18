package com.swetlox_app.swetlox.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "usercollections")
@Data
public class UserCollection {
    @Id
    private String id;
    private String userId;
    private List<String> reelList=new ArrayList<>();
    private List<String> postList=new ArrayList<>();
    private LocalDateTime savedAt;
}
