package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.service.UserService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class GitHubAuthentication implements OAuthAuthenticationStatrgey{
    private final static String CLIENT_ID="";
    private final static String CLIENT_SECRET="";
    private final static String TOKEN_URI="https://github.com/login/oauth/access_token";
    private final static String FETCH_DATA_URI = "https://api.github.com/user";
    private final RestTemplate restTemplate;
    private final UserService userService;

    public GitHubAuthentication(RestTemplate restTemplate, UserService userService){
        this.restTemplate = restTemplate;
        this.userService=userService;
    }

    @Override
    public void authenticate(String code) {
        Map<String, String> params = Map.of("client_id", CLIENT_ID, "client_secret", CLIENT_SECRET, "code", code);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URI, HttpMethod.POST, request, Map.class);
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
