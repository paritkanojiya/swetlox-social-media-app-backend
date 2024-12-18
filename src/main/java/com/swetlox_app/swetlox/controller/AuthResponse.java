package com.swetlox_app.swetlox.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Builder
@Data
public class AuthResponse {
    private String message;
    private HttpStatus httpStatus;
}