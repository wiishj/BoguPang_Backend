package com.kill.gaebokchi.domain.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {
    private Key key;
    public JWTUtil(@Value("${jwt.secret}")String secret){
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        key= Keys.hmacShaKeyFor(byteSecretKey);
    }

    public String getUsername(String accessToken){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().get("username", String.class);
    }
    public String getRole(String accessToken){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().get("role", String.class);

    }
    public Boolean isExpired(String accessToken){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration().before(new Date());
    }
    public String createJwt(String username, String role, Long expiredMs) {

        Claims claims = Jwts.claims();
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}