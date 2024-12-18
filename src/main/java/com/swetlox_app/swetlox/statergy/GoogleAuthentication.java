package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class GoogleAuthentication implements OAuthAuthenticationStatrgey{


    private final static String CLIENT_ID="";
    private final static String CLIENT_SECRET="";
    private final static String TOKEN_URI="https://oauth2.googleapis.com/token";
    private final static String FETCH_DATA_URI="https://www.googleapis.com/oauth2/v3/userinfo";


    private final RestTemplate restTemplate;
    private final UserService userService;

    public GoogleAuthentication(RestTemplate restTemplate, UserService userService){
        this.restTemplate = restTemplate;
        this.userService=userService;
    }
    @Override
    public void authenticate(String code) {
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
        fetchUserData(response.getBody().get("access_token").toString());
    }
    public void fetchUserData(String token){
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
            UserDto userDto = UserDto.builder()
                    .userName(name)
                    .fullName(name)
                    .email(email)
                    .roleList(List.of(role))
                    .build();
            userService.saveOAut2User(userDto);
        }
    }
}
