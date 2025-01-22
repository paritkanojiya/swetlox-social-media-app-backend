package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.service.OAuth2Service;
import com.swetlox_app.swetlox.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

public class OAuth2Factory {


    public static OAuthAuthenticationStatrgey getInstance(String oAuth2,ApplicationContext applicationContext){
        switch (oAuth2){
            case "GITHUB"->{
                return applicationContext.getBean("GitHubAuthentication", GitHubAuthentication.class);
            }
            case "GOOGLE"->{
                return applicationContext.getBean("GoogleAuthentication", GoogleAuthentication.class);
            }
            default -> {
                throw new RuntimeException("NOT OAUTH2 FOUND");
            }
        }
    }
}
