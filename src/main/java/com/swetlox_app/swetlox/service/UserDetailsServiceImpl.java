package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("email {}",username);
        User user = userService.getUser(username);
        log.info("user {}",user);
        if(user==null)
            throw new UsernameNotFoundException(username+ " is not found");
        return new UserDetailsImpl(user);
    }
}
