package com.swetlox_app.swetlox.exception;

import com.swetlox_app.swetlox.controller.AuthResponse;
import com.swetlox_app.swetlox.exception.customException.InvalidOtpEx;
import com.swetlox_app.swetlox.exception.customException.InvalidPasswordEx;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(InvalidOtpEx.class)
    public ResponseEntity<AuthResponse> invalidOtpEx(InvalidOtpEx ex){
        AuthResponse response = AuthResponse.builder()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserAlreadyExistEx.class)
    public ResponseEntity<AuthResponse> userAlreadyExistEx(UserAlreadyExistEx ex){
        AuthResponse response = AuthResponse.builder()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.CONFLICT)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidPasswordEx.class)
    public ResponseEntity<AuthResponse> passwordNotMatch(InvalidPasswordEx ex){
        AuthResponse response = AuthResponse.builder()
                .message(ex.getMessage())
                .httpStatus(HttpStatus.CONFLICT)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
