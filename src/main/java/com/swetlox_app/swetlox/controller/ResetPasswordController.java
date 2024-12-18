package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.ForgetPasswordService;
import com.swetlox_app.swetlox.service.UserConnectionService;
import com.swetlox_app.swetlox.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final UserService userService;
    private final ForgetPasswordService forgetPasswordService;

    @GetMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestHeader("Authorization") String token) throws MessagingException {
        User authUser = userService.getAuthUser(token);
        forgetPasswordService.resetTokenURL(authUser);
        return ResponseEntity.ok("reset link send on your email");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String resetToken,@RequestHeader("Authorization") String token){
        User authUser = userService.getAuthUser(token);
        if(forgetPasswordService.validateToken(authUser.getId(),resetToken)){
            return ResponseEntity.ok("reset link is activate");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("reset link expire or not valid");
    }
}
