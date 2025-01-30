package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.ForgotPasswordDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.ForgetPasswordService;
import com.swetlox_app.swetlox.service.UserConnectionService;
import com.swetlox_app.swetlox.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final UserService userService;
    private final ForgetPasswordService forgetPasswordService;

    @GetMapping("/forget-password/{email}")
    public ResponseEntity<?> forgetPassword(@PathVariable("email") String email) throws MessagingException {
        try{
            User user = userService.getUser(email);
            forgetPasswordService.resetTokenURL(user);
            return ResponseEntity.ok("reset link send on your email");
        }catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordDto forgotPasswordDto){
        try{
            forgetPasswordService.validateToken(forgotPasswordDto);
            return ResponseEntity.ok("reset link is activate");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("reset link expire or not valid");
        }
    }
}
