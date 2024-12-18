package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.constant.App;
import com.swetlox_app.swetlox.entity.ForgetPassword;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.repository.ForgetPasswordRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgetPasswordService {

    private final ForgetPasswordRepo forgetPasswordRepo;
    private final MailService mailService;

    public void resetTokenURL(User authUser) throws MessagingException {
        ForgetPassword forgetPassword = forgetPasswordRepo.findByUserId(authUser.getId()).orElseGet(() -> createForgetPassword(authUser));
        forgetPassword.setToken(generateToken());
        forgetPassword.setExpiryTime(LocalDateTime.now().plusMinutes(30));
        ForgetPassword savedForgetPassword = forgetPasswordRepo.save(forgetPassword);
        String resetLink= App.FRONTENDCONTEXTPATH+"/v1/reset-password?token="+savedForgetPassword.getToken();
        mailService.sendResetLink(authUser.getEmail(),resetLink);
    }
    private ForgetPassword createForgetPassword(User authUser){
        return ForgetPassword.builder()
                .userId(authUser.getId())
                .build();
    }
    private String generateToken(){
        return UUID.randomUUID().toString();
    }

    public boolean validateToken(String authId,String token){
        ForgetPassword authUserForgetPassword = forgetPasswordRepo.findByUserId(authId).orElse(null);
        return authUserForgetPassword != null && authUserForgetPassword.getToken().equals(token) && !authUserForgetPassword.getExpiryTime().isBefore(LocalDateTime.now());
    }
}
