package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.service.OAuth2Service;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(value = "GoogleAuthentication")
@Scope("prototype")
public class GoogleAuthentication implements OAuthAuthenticationStatrgey{

    @Value("${oAuth2.google.clientId}")
    private String CLIENT_ID;
    @Value("${oAuth2.google.client-secret}")
    private String CLIENT_SECRET;
    @Value("${oAuth2.google.token-uri}")
    private String TOKEN_URI;
    @Value("${oAuth2.google.fetch-data-uri}")
    private String FETCH_DATA_URI;


    private final RestTemplate restTemplate;
    private final OAuth2Service oAuth2Service;

    public GoogleAuthentication(RestTemplate restTemplate, OAuth2Service oAuth2Service){
        this.restTemplate = restTemplate;
        this.oAuth2Service=oAuth2Service;
    }

    @Override
    public User authenticate(String code) throws UserAlreadyExistEx, MessagingException {


            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", "http://localhost:9000/swetlox/v1/auth/login/google");
            HttpHeaders headers=new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URI, HttpMethod.POST, request, Map.class);
            System.out.println(response);
            return fetchUserData(Objects.requireNonNull(response.getBody()).get("access_token").toString());
    }

    private User fetchUserData(String token) throws UserAlreadyExistEx, MessagingException {
        System.out.println(token);
        HttpHeaders headers=new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Object> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(FETCH_DATA_URI, HttpMethod.GET, request, Map.class);
        if(response.getStatusCode().is2xxSuccessful()){
            Map responseBody = response.getBody();
            assert responseBody != null;
            String name = (String) responseBody.get("name");
            String email = (String) responseBody.get("email");
            Role role = new Role();
            role.setRole("ROLE_USER");
            UserDetailsDto userDto = UserDetailsDto.builder()
                    .userName(name)
                    .fullName(name)
                    .userType(UserType.GOOGLE)
                    .email(email)
                    .roleList(List.of(role))
                    .build();
           return oAuth2Service.saveOAut2User(userDto);
        }
        throw new RuntimeException("Something went wrong in OAuth2");
    }
}
