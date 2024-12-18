package com.swetlox_app.swetlox.config.authentication;


import com.swetlox_app.swetlox.exception.customException.InvalidPasswordEx;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@Slf4j
public class UserPasswordAuthentication implements AuthenticationProvider {
    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("authentication process {}",authentication.getName());
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        log.info("password {}",userDetails.getPassword());
        log.info("authpassword {}",(String)authentication.getCredentials());
        if(userDetails!=null){
            if(passwordEncoder.matches((String)authentication.getCredentials(),userDetails.getPassword())){
                return new UsernamePasswordAuthenticationToken(userDetails.getUsername(),null,userDetails.getAuthorities());
            }
            throw new InvalidPasswordEx("password not match");
        }
        throw new BadCredentialsException("User not found");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
