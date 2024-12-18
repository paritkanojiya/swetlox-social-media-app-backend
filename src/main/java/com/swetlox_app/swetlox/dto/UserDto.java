package com.swetlox_app.swetlox.dto;

import com.swetlox_app.swetlox.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private String profileURL;
    private List<Role> roleList;
}