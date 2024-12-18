package com.swetlox_app.swetlox.entity;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "users")
@Builder
public class User {
    @Id
    private String id;
    private String fullName;
    private String userName;
    @Indexed(unique = true)
    private String email;
    private String password;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private String profileURL;
    private List<String> bio;
    private List<Role> roleList;
    private boolean isOAuth2User;
}
