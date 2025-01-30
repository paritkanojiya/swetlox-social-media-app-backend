package com.swetlox_app.swetlox.statergy;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.service.OAuth2Service;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(value = "GitHubAuthentication")
@Scope("prototype")
@Slf4j
public class GitHubAuthentication implements OAuthAuthenticationStatrgey{

    @Value("${oAuth2.github.clientId}")
    private String CLIENT_ID;
    @Value("${oAuth2.github.client-secret}")
    private String CLIENT_SECRET;
    @Value("${oAuth2.github.token-uri}")
    private String TOKEN_URI;
    @Value("${oAuth2.github.fetch-data-uri}")
    private String FETCH_DATA_URI;
    private final RestTemplate restTemplate;
    private final OAuth2Service oAuth2Service;

    public GitHubAuthentication(RestTemplate restTemplate, OAuth2Service oAuth2Service){
        this.restTemplate = restTemplate;
        this.oAuth2Service=oAuth2Service;
    }

    @Override
    public User authenticate(String code) throws UserAlreadyExistEx, MessagingException {

            log.info("{} {} {}",CLIENT_ID,CLIENT_SECRET,code);
            Map<String, String> params = Map.of("client_id", CLIENT_ID, "client_secret", CLIENT_SECRET, "code", code);
            HttpHeaders headers=new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URI, HttpMethod.POST, request, Map.class);
            System.out.println(response);
            return fetchUserData(Objects.requireNonNull(response.getBody()).get("access_token").toString());

    }

    private User fetchUserData(String token) throws UserAlreadyExistEx, MessagingException {
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
                    .email(email)
                    .userType(UserType.GITHUB)
                    .roleList(List.of(role))
                    .build();
            return oAuth2Service.saveOAut2User(userDto);
        }
        throw new RuntimeException("Something went wrong in OAuth2");
    }
}
