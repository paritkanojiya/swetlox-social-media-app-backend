package com.swetlox_app.swetlox.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.exception.customException.InvalidOtpEx;
import com.swetlox_app.swetlox.exception.customException.UserAlreadyExistEx;
import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.service.JwtService;
import com.swetlox_app.swetlox.service.MailService;
import com.swetlox_app.swetlox.service.UserService;
import com.swetlox_app.swetlox.statergy.OAuth2Factory;
import com.swetlox_app.swetlox.statergy.OAuthAuthenticationStatrgey;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager manager;
    private final MailService otpService;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final ApplicationContext applicationContext;
   

    @PostMapping("/sign-up")
    public ResponseEntity<User> signUp(@RequestBody UserDto userDto) throws MessagingException, UserAlreadyExistEx {
        User savedUser = userService.saveUser(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/otp/verify/{email}/{otp}")
    public ResponseEntity<AuthResponse> verifyOtp(@PathVariable("email") String email, @PathVariable("otp") String otp, HttpServletResponse response) throws InvalidOtpEx {

        otpService.validateOtp(otp);
        User user = userService.getUser(email);
        user.setIsVerified(true);
        userService.updateUser(user);

        AuthResponse authResponse = AuthResponse.builder().message("verification complete")
                .httpStatus(HttpStatus.OK)
                .build();
        otpService.clearCache();
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        log.info("email {} password {}",request.getEmail(),request.getPassword());
        Authentication authenticated = manager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        log.info("authenticated");
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticated.getName());
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        String token = jwtService.generateToken(userDetails);
        AuthResponse authResponse = AuthResponse.builder().message(token)
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/login/{regiId}")
    public RedirectView oAuthLogin(@RequestParam("code") String code, @PathVariable String regiId){
        log.info("OAuth2 info {}",code);
        OAuthAuthenticationStatrgey  authenticationStatrgey = OAuth2Factory.getInstance(regiId.toUpperCase(),applicationContext);
        authenticationStatrgey.authenticate(code);
        return new RedirectView("http://localhost:3000/");
    }

    @GetMapping("/login")
    public RedirectView ok(){
        String authorizationUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + "Ov23liw87lyR9NqxEsVr";
        return new RedirectView(authorizationUrl);
    }
}
