package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.domain.Member; // Member 엔티티의 정확한 경로를 확인하세요
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // ⭐️ 구현할 인터페이스 ⭐️
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security가 사용하는 UserDetails 구현체

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * Spring Security의 핵심 메서드: 사용자 ID를 기반으로 DB에서 회원 정보를 로드합니다.
     * @param memberId 로그인 폼에서 제출된 ID (Username)
     * @return UserDetails (Spring Security가 인증 및 권한 확인에 사용하는 객체)
     * @throws UsernameNotFoundException 해당 ID의 회원이 DB에 없을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        return memberRepository.findByMemberId(memberId)
                // DB에서 Member 엔티티를 찾으면 UserDetails 객체로 변환합니다.
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("회원 ID [" + memberId + "]를 찾을 수 없습니다."));
    }

    /**
     * Member 엔티티 정보를 Spring Security의 UserDetails 객체로 변환합니다.
     */
    private UserDetails createUserDetails(Member member) {

        // 1. 권한 설정
        // Spring Security는 권한 이름 앞에 "ROLE_" 접두사가 붙어야 합니다.
        // member.getRole().name()이 ADMIN, USER 등을 반환한다고 가정합니다.
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + member.getRole().name());

        // 2. UserDetails 객체 반환
        return new User(
                member.getMemberId(), // 사용자 ID
                member.getPassword(), // DB에 저장된 암호화된 비밀번호
                Collections.singleton(authority) // 권한 목록
        );
    }
}