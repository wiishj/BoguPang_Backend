package com.kill.gaebokchi.domain.account.jwt;

import com.kill.gaebokchi.domain.account.security.CustomUserDetailsService;
import com.kill.gaebokchi.global.error.BadRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static com.kill.gaebokchi.global.error.ExceptionCode.*;

@Component
@Slf4j
public class JWTUtil {
    private final Key key;
    private final CustomUserDetailsService customUserDetailsService;
    public JWTUtil(CustomUserDetailsService customUserDetailsService, @Value("${jwt.secret}")String secret){
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        key= Keys.hmacShaKeyFor(byteSecretKey);
        this.customUserDetailsService = customUserDetailsService;
    }

    public String getSubject(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String getCategory(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("category", String.class);
    }
    public String getRole(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public Authentication getAuthentication(String accessToken){
        String email = getSubject(accessToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String createJwt(String category, String role, String subject, Long expiredMs) {

        return Jwts.builder()
                .claim("category", category)
                .claim("role", role)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public boolean validate(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)  //검증키 지정
                    .build()
                    .parseClaimsJws(token); //토큰의 유효 기간을 확인하기 위해 exp claim을 가져와 현재와 비교
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 잘못 되었습니다.");
        }
        return false;
    }
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
