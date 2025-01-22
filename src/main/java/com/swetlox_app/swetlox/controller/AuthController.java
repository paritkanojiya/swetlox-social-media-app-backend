package com.swetlox_app.swetlox.controller;


import com.swetlox_app.swetlox.dto.AuthRequest;
import com.swetlox_app.swetlox.dto.AuthResponse;
import com.swetlox_app.swetlox.dto.user.UserDetailsDto;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.InvalidOtpEx;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.service.JwtService;
import com.swetlox_app.swetlox.service.MailService;
import com.swetlox_app.swetlox.service.OAuth2Service;
import com.swetlox_app.swetlox.service.UserService;
import com.swetlox_app.swetlox.statergy.OAuth2Factory;
import com.swetlox_app.swetlox.statergy.OAuthAuthenticationStatrgey;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {

    @Value("${swetlox.fronted-path}")
    private String FRONTED_PATH;

    private final UserService userService;
    private final AuthenticationManager manager;
    private final MailService otpService;
    private final JwtService jwtService;
    private final ApplicationContext applicationContext;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/sign-up")
    public ResponseEntity<User> signUp(@RequestBody UserDetailsDto userDto) throws MessagingException, UserAlreadyExistEx {
        User savedUser = userService.saveUser(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/otp/verify/{email}/{otp}")
    public ResponseEntity<AuthResponse> verifyOtp(@PathVariable("email") String email, @PathVariable("otp") String otp) throws InvalidOtpEx {
            otpService.validateOtp(otp);
            userService.changeVerificationStatus(email);
            AuthResponse authResponse = AuthResponse.builder().message("verification complete")
                    .httpStatus(HttpStatus.OK)
                    .build();
            otpService.clearCache();
            return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request) {
        log.info("email {} password {}",request.getEmail(),request.getPassword());
        Authentication authenticated = manager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        log.info("authenticated");
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        String token = jwtService.generateToken(authenticated.getName());
        AuthResponse authResponse = AuthResponse.builder().message(token)
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/login/{regiId}")
    public RedirectView oAuthLogin(@RequestParam("code") String code, @PathVariable String regiId){
        log.info("OAuth2 info {}",code);
        try{
            OAuthAuthenticationStatrgey authenticationStrategy = OAuth2Factory.getInstance(regiId.toUpperCase(),applicationContext);
            User oAuth2User = authenticationStrategy.authenticate(code);
            String generatedToken = jwtService.generateToken(oAuth2User.getEmail());
            return new RedirectView(FRONTED_PATH+"?authToken="+generatedToken);
        }catch (UserAlreadyExistEx | Exception e){
            return new RedirectView(FRONTED_PATH);
        }
    }

    @GetMapping("/oauth/login/{oAuthProviderName}")
    public RedirectView oAuth2Login(@PathVariable String oAuthProviderName){
        String authorizationUrl = oAuth2Service.oAuth2Login(oAuthProviderName);
        return new RedirectView(authorizationUrl);
    }
}
