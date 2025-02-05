package com.example.ninetynine.domain.member.service;

import com.example.ninetynine.api.dto.LoginRequestDto;
import com.example.ninetynine.api.dto.OnboardingRequestDto;
import com.example.ninetynine.api.dto.SignupRequestDto;
import com.example.ninetynine.domain.member.entity.Member;
import com.example.ninetynine.domain.member.entity.MemberInfo;
import com.example.ninetynine.domain.member.repository.MemberInfoRepository;
import com.example.ninetynine.domain.member.repository.MemberRepository;
import com.example.ninetynine.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    // 회원가입 signup
    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        // 회원 중복 확인
        Optional<Member> found = memberRepository.findByEmail(email);
        if (found.isPresent()) {
            throw new IllegalArgumentException("중복된 이메일이 존재합니다.");
        }

        Member member = new Member(email, password);
        memberRepository.save(member);
    }

    // 회원가입 onboarding
    @Transactional
    public void onboarding(OnboardingRequestDto onboardingRequestDto, HttpServletRequest request) {
        String firstName = onboardingRequestDto.getFirstName();
        String lastName = onboardingRequestDto.getLastName();
        String userName = onboardingRequestDto.getUserName();
        String profilePic = onboardingRequestDto.getProfilePic();
        String email = onboardingRequestDto.getEmail();

//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        if (token != null) {
//            if (jwtUtil.validateToken(token)) {
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//            Member member = memberRepository.findByEmail(claims.getSubject()).orElseThrow(
//                    () -> new IllegalArgumentException("email이 존재하지 않습니다.")
//            );
//
//            member.update(firstName, lastName, userName);
//
//        }

         Member member = (Member) memberRepository.findMemberByEmail(email).orElseThrow(
                 () -> new IllegalArgumentException("email이 존재하지 않습니다.")
         );

         member.update(firstName, lastName, userName, profilePic, email);


    }



    // 로그인
    @Transactional(readOnly = true)
    public void login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        // 유저 이메일, 비밀번호 가져오기
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // email 확인
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("등록된 email이 없습니다.")
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(member.getEmail()));
    }

}
