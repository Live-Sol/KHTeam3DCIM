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
     */
    private final Member member;

    /**
     * Spring Security 권한 정보
     */
    private final Collection<? extends GrantedAuthority> authorities;

    // ⭐️ [추가 1] 프로필 이미지 경로를 담을 변수
    private String profileImage;

    /**
     * 로그인 성공 시 생성자
     */
    public CustomUserDetails(Member member,
                             Collection<? extends GrantedAuthority> authorities) {
        this.member = member;
        this.authorities = authorities;

        // ⭐️ [추가 2] Member 엔티티에서 이미지 경로를 꺼내와 저장
        // (주의: Member.java에 getProfileImage() 메서드가 있어야 오류가 안 납니다!)
        this.profileImage = member.getProfileImage();
    }

    // ⭐️ [추가 3] 헤더(HTML)에서 이미지를 꺼내 쓰기 위한 메서드
    public String getProfileImage() {
        return profileImage;
    }


    // --- 아래는 기존 코드와 동일합니다 ---

    /**
     * 로그인 ID (Spring Security에서 username으로 사용)
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
     * 사용자 실명 / 이름
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

    // ===== 계정 상태 관련 =====

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}