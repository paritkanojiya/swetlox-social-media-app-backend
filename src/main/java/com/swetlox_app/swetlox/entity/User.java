package com.swetlox_app.swetlox.entity;


import com.swetlox_app.swetlox.allenum.UserType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private UserType userType;
    private Boolean isVerified;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime lastModifyDate;
    private String profileURL;
    private List<String> bio;
    private List<Role> roleList;
}
