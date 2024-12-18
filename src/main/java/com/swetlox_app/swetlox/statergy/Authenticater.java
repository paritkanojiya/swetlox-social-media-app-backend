package com.swetlox_app.swetlox.statergy;

import org.springframework.stereotype.Service;

@Service
public class Authenticater {
    private OAuthAuthenticationStatrgey authAuthenticationStatrgey;

    public void setAuthAuthenticationStatrgey(OAuthAuthenticationStatrgey authAuthenticationStatrgey){
        this.authAuthenticationStatrgey=authAuthenticationStatrgey;
    }

    public void authenticate(String code){
        authAuthenticationStatrgey.authenticate(code);
    }
}
