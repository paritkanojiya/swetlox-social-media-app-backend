package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.UserType;

import com.swetlox_app.swetlox.dto.ForgotPasswordDto;
import com.swetlox_app.swetlox.entity.ForgetPassword;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.repository.ForgetPasswordRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgetPasswordService {

    private final ForgetPasswordRepo forgetPasswordRepo;
    private final UserService userService;
    private final MailService mailService;
    private final OAuth2Service oAuth2Service;

    @Value("${fronted.forgot-password.url}")
    private String forgotPasswordURL;

    public void resetTokenURL(User authUser) throws MessagingException {
        if(!authUser.getUserType().equals(UserType.EMAIL)) throw new RuntimeException("provided email id register with social login try login with social login");
        ForgetPassword forgetPassword = forgetPasswordRepo.findByUserId(authUser.getId()).orElseGet(() -> createForgetPassword(authUser));
        forgetPassword.setToken(generateToken());
        forgetPassword.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        ForgetPassword savedForgetPassword = forgetPasswordRepo.save(forgetPassword);
        String resetLink= forgotPasswordURL+"?token="+savedForgetPassword.getToken();
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

    public void validateToken(ForgotPasswordDto forgetPasswordDto){

        Optional<ForgetPassword> optionalForgetPassword =forgetPasswordRepo.findByToken(forgetPasswordDto.getToken());
        if(optionalForgetPassword.isPresent()){
            ForgetPassword forgetPassword = optionalForgetPassword.get();
            User user = userService.getUserById(forgetPassword.getUserId());
            if((!forgetPassword.getExpiryTime().isBefore(LocalDateTime.now())) && user.getUserType().equals(UserType.EMAIL)){
                userService.updatePassword(user,forgetPasswordDto.getNewPassword());
                return;
            }
            throw new RuntimeException("your email id is register with social login try with social login");
        }
        throw new RuntimeException("invalid token");
    }
}
