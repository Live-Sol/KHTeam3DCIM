package com.example.KHTeam3DCIM.config;

import com.example.KHTeam3DCIM.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // ⭐️ BCryptPasswordEncoder를 Bean으로 등록 ⭐️
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. URL 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 회원가입, 로그인 페이지 및 정적 리소스(CSS/JS)는 모두 허용
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", // 정적 리소스
                                "/",
                                "/members/signup",      // 회원가입
                                "/members/check-id",    // 아이디 중복 확인
                                "/members/login",       // 로그인
                                "/members/forgot-password", // 비밀번호 찾기
                                "/members/send-verification-code", // 인증 코드 발송
                                "/members/verify-code", // 인증 코드 검증
                                "/solutions/**",        // 솔루션 페이지 전체 공개
                                "/info/**",             // 정보 페이지 전체 공개
                                "/specs/**",            // 제원 페이지 전체 공개
                                "/admin/api/env/now" // ⭐️ [추가] API는 JS에서 호출하므로 허용 (필요시 authenticated로 변경 가능)
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/members").permitAll()
                        // 관리자 페이지는 'ADMIN' 역할을 가진 사용자만 접근 허용
                        .requestMatchers("/members/admin/**", "/admin/**").hasRole("ADMIN")
                        // 그 외 모든 요청은 인증된 사용자(로그인한 사용자)에게만 허용
                        .anyRequest().authenticated()
                )

                // 2. 로그인 설정
                .formLogin(form -> form
                        // 커스텀 로그인 페이지 URL 지정 (기존의 /members/login 사용)
                        .loginPage("/members/login")
                        // 로그인 처리 URL 지정 (POST /members/login)
                        // MemberController의 @PostMapping("/login") 메서드를 Spring Security가 가로채서 처리합니다.
                        .loginProcessingUrl("/members/login")
                        .usernameParameter("memberId")
                        // 로그인 성공 후 리다이렉트될 기본 URL
                        .defaultSuccessUrl("/", true)
                        // 로그인 실패 시 리다이렉트될 URL
                        .failureUrl("/members/login?error")
                        .permitAll()
                )

                // 3. 로그아웃 설정
                .logout(logout -> logout
                        // 로그아웃 처리 URL 지정
                        .logoutUrl("/members/logout")
                        // 로그아웃 성공 시 리다이렉트될 URL
                        .logoutSuccessUrl("/")
                        // 세션 무효화
                        .invalidateHttpSession(true)
                        .permitAll()
                )

                // CSRF 설정을 해제하지 않으면 POST 요청에 문제가 생길 수 있지만,
                // 현재는 Spring Security의 기본 설정을 따르는 것이 안전합니다.
                // .csrf(csrf -> csrf.disable());

                // HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}