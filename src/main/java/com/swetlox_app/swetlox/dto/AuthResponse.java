package com.swetlox_app.swetlox.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Builder
@Data
public class AuthResponse {
    private String message;
    private HttpStatus httpStatus;
}