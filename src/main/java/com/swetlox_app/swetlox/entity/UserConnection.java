package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;

import java.util.List;

@Collation(value = "userConnections")
@Getter
@Setter
public class UserConnection {
    @Id
    private String id;
    private String userId;
    private List<String> follower;
    private List<String> following;
}
