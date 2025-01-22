package com.swetlox_app.swetlox.dto.user;

import com.swetlox_app.swetlox.allenum.UserType;
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
public class UserDetailsDto {
    private String id;
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private String profileURL;
    private UserType userType;
    private List<Role> roleList;
}