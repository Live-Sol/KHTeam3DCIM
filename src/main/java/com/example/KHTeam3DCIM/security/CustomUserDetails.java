package com.example.KHTeam3DCIM.security;
import com.example.KHTeam3DCIM.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * 실제 회원 정보 (DB의 Member 엔티티)
     * → memberId, name, role 등 모든 컬럼 접근 가능
     */
    private final Member member;

    /**
     * Spring Security 권한 정보
     * → ROLE_USER, ROLE_ADMIN 등
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 로그인 성공 시
     * CustomUserDetailsService에서 생성되어
     * SecurityContext에 저장되는 인증 객체
     */
    public CustomUserDetails(Member member,
                             Collection<? extends GrantedAuthority> authorities) {
        this.member = member;
        this.authorities = authorities;
    }

    /**
     * 로그인 ID (Spring Security에서 username으로 사용)
     * → memberId 반환
     */
    @Override
    public String getUsername() {
        return member.getMemberId();
    }

    /**
     * 암호화된 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /**
     * ⭐ 사용자 실명 / 이름
     * → Thymeleaf: sec:authentication="principal.name"
     */
    public String getName() {
        return member.getName();
    }

    /**
     * 권한 목록 반환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // ===== 계정 상태 관련 (필요 시 확장 가능) =====

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}