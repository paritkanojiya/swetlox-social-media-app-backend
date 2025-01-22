package com.swetlox_app.swetlox.dto;

import lombok.Data;

@Data
public class ForgotPasswordDto {
    private String token;
    private String newPassword;
}
