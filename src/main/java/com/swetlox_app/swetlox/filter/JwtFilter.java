package com.swetlox_app.swetlox.filter;

import com.swetlox_app.swetlox.service.JwtService;
import com.swetlox_app.swetlox.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null){
            String token=header.substring(7);
            try{
                jwtService.validateToken(token);
                String userName=jwtService.extractUserNameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authenticated=UsernamePasswordAuthenticationToken.authenticated(userDetails.getUsername(),null,userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticated);
            }catch (Exception ex){
                log.error("Exception occur");
            }
        }
        filterChain.doFilter(request,response);
    }
}
