package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class Authenticater {
    private OAuthAuthenticationStatrgey authAuthenticationStatrgey;

    public void setAuthAuthenticationStatrgey(OAuthAuthenticationStatrgey authAuthenticationStatrgey){
        this.authAuthenticationStatrgey=authAuthenticationStatrgey;
    }

    public void authenticate(String code) throws UserAlreadyExistEx, MessagingException {
        authAuthenticationStatrgey.authenticate(code);
    }
}
