package com.kill.gaebokchi.domain.user.service;

import com.kill.gaebokchi.domain.user.dto.MemberDto;
import com.kill.gaebokchi.domain.user.dto.SignUpDto;
import com.kill.gaebokchi.domain.user.jwt.JwtTokenProvider;
import com.kill.gaebokchi.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public String login(String username, String password){
        //1. create Authentication object using profileId and password
        //value of authenticated = false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        //2. check validation of Member using method authenticate()
        //authenticate() -> loadUserByUsername(made by CustomUserDetailService)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //3. create JWT token using authentication
        return jwtTokenProvider.generateToken(authentication);

    }
    @Transactional
    public MemberDto signUp(SignUpDto signUpDto){
        if(memberRepository.existsByUsername(signUpDto.getUsername())){
            throw new IllegalStateException("이미 사용 중인 사용자 이름입니다.");
        }
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        return MemberDto.toDto(memberRepository.save(signUpDto.toEntity(encodedPassword, roles)));
    }
}
