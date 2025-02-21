package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private final User user;
    public UserDetailsImpl(User user){
        this.user=user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoleList().stream().map(role-> new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public boolean isAuth2User(){
        return List.of(UserType.GOOGLE,UserType.GITHUB).contains(user.getUserType());
    }

    public boolean isVerified(){
        return user.getIsVerified();
    }
    public boolean isSuspense(){
        return user.isSuspend();
    }
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
