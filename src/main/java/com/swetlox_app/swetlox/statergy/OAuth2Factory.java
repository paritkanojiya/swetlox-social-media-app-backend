package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

public class OAuth2Factory {


    public static OAuthAuthenticationStatrgey getInstance(String oAuth2,ApplicationContext applicationContext){
        UserService userService = applicationContext.getBean("userService", UserService.class);
        RestTemplate restTemplate = applicationContext.getBean("restTemplate", RestTemplate.class);
        switch (oAuth2){
            case "GITHUB"->{
                return new GitHubAuthentication(restTemplate,userService);
            }
            case "GOOGLE"->{
                return new GoogleAuthentication(restTemplate,userService);
            }
            default -> {
                throw new RuntimeException("NOT OAUTH2 FOUND");
            }
        }
    }
}
