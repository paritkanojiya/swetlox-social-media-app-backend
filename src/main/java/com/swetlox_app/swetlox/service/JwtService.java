package com.swetlox_app.swetlox.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.expiry-time}")
    private Integer EXPIRY_TIME;
    @Value("${jwt.issuer}")
    private String issuer;

    public String generateToken(String userEmail){
        return Jwts.builder().subject(userEmail)
                .signWith(key())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+EXPIRY_TIME))
                .issuer(issuer)
                .compact();
    }

    public void validateToken(String token)  {
        try{
            Jwts.parser().verifyWith(key())
                    .build().parseSignedClaims(token);
        }
        catch (JwtException ex){
            throw new JwtException("Token expire or invalid");
        }
    }
    public String extractUserNameFromToken(String token){
        return Jwts.parser().verifyWith(key())
                .build().parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractTokenFromHeader(HttpServletRequest request)  {
        return request.getHeader("Authorization");
    }
    private SecretKey key(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
