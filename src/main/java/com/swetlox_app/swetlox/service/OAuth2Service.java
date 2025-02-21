package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {


    private final UserService userService;
    private final Environment environment;
    private final UserPreferenceService userPreferenceService;

    public User saveOAut2User(UserDetailsDto user) {
        if(user.getEmail()==null) throw new RuntimeException("User email can not be null");
        boolean userExist = userService.isUserExistByEmail(user.getEmail());
        boolean isUserNameExist=userService.isUserNameExist(user.getUserName());
        if(!userExist) {
            if(isUserNameExist) throw new RuntimeException("userName already exist");
            return userService.saveOAuth2User(user);
        }
        User auth2User = userService.getUser(user.getEmail());
        if(auth2User.isSuspend()) throw new RuntimeException("you are blacklist user");
        if(auth2User.getUserType().equals(UserType.EMAIL)) throw new RuntimeException("provided email already register");
        if(!auth2User.getUserType().equals(user.getUserType()))  auth2User.setUserType(user.getUserType());
        userService.updateUser(auth2User);
        return auth2User;
    }

    public String oAuth2Login(String oauth2LoginId){
        oauth2LoginId=oauth2LoginId.trim().toUpperCase();
        switch (oauth2LoginId){
            case "GOOGLE"->{
                return getGoogleOauth2LoginURI();
            }
            case "GITHUB"->{
                return getGithubOauth2LoginURI();
            }
            default ->
                throw new RuntimeException("No OAuth2 provider found");
        }
    }


    private String getGoogleOauth2LoginURI(){
        String googleLoginURI = environment.getProperty("oAuth2.google.oauth2-login-uri");
        String clientId = environment.getProperty("oAuth2.google.clientId");
        return googleLoginURI+"?client_id="+clientId+"&redirect_uri=http://localhost:9000/swetlox/v1/auth/login/google&response_type=code&scope=email%20profile";
    }

    private String getGithubOauth2LoginURI(){
        String googleLoginURI = environment.getProperty("oAuth2.github.oauth2-login-uri");
        String clientId = environment.getProperty("oAuth2.github.clientId");
        return googleLoginURI+"?client_id="+clientId;
    }
}
