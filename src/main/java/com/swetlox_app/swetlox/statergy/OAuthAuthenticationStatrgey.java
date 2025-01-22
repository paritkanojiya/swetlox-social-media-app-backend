package com.swetlox_app.swetlox.statergy;


import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import jakarta.mail.MessagingException;

public interface OAuthAuthenticationStatrgey {
    User authenticate(String code) throws UserAlreadyExistEx, MessagingException;
}
