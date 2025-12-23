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
                // 1️⃣ URL 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize

                        // ⭐ OPTIONS 요청 허용
                        // fetch API가 POST 요청 시 브라우저가 먼저 보내는 프리플라이트 요청을 허용
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // 정적 리소스(CSS, JS, 이미지) 및 모든 사용자가 접근 가능한 페이지 허용
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",    // 정적 리소스
                                "/",                                  // 메인 페이지
                                "/members/signup",                    // 회원가입 페이지
                                "/members/check-id",                  // 회원가입 아이디 중복 확인 API
                                "/members/login",                     // 로그인 페이지
                                "/members/forgot-password",           // 비밀번호 찾기 페이지
                                "/members/send-verification-code",    // 인증 코드 발송 API
                                "/members/verify-code",               // 인증 코드 검증 API
                                "/solutions/**",                      // 솔루션 페이지 전체 공개
                                "/info/**",                           // 정보 페이지 전체 공개
                                "/specs/**",                          // 제원 페이지 전체 공개
                                "/admin/api/env/now",                 // JS에서 호출하는 API 허용
                                "/devices/batch-update",              // 장비 선택 수정 API
                                "/devices/batch-delete"               // 장비 선택 삭제 API
                        ).permitAll()

                        // 🔒 회원정보 수정 전 비밀번호 확인 API는 로그인된 사용자만 접근 가능
                        .requestMatchers("/members/check-password").authenticated()

                        // POST /members (회원가입 등) 요청은 모든 사용자 허용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/members").permitAll()

                        // 관리자 페이지는 ADMIN 역할만 접근 가능
                        .requestMatchers("/members/admin/**", "/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증된 사용자만 허용
                        .anyRequest().authenticated()
                )

                // 2️⃣ 로그인 설정
                .formLogin(form -> form
                        .loginPage("/members/login")               // 커스텀 로그인 페이지 URL
                        .loginProcessingUrl("/members/login")     // 로그인 POST 요청 처리 URL
                        .usernameParameter("memberId")            // 로그인 폼에서 사용할 username 파라미터
                        .defaultSuccessUrl("/", true)             // 로그인 성공 후 리다이렉트할 기본 URL
                        .failureUrl("/members/login?error")       // 로그인 실패 시 리다이렉트할 URL
                        .permitAll()                               // 로그인 페이지는 모든 사용자 접근 허용
                )

                // 3️⃣ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/members/logout")            // 로그아웃 처리 URL
                        .logoutSuccessUrl("/")                    // 로그아웃 후 리다이렉트할 URL
                        .invalidateHttpSession(true)             // 세션 무효화
                        .permitAll()                              // 로그아웃 기능은 모든 사용자 접근 허용
                )

                // 4️⃣ CSRF 설정
                .csrf(csrf -> csrf
                        // 비밀번호 확인 API는 fetch POST + JSON으로 호출되므로 CSRF 검증 제외
                        .ignoringRequestMatchers("/members/check-password")
                )

                // 5️⃣ HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

                // 6️⃣ 커스텀 UserDetailsService 등록
                // 로그인 시 DB에서 Member 정보를 가져와 인증/권한 처리
                .userDetailsService(customUserDetailsService);

        // SecurityFilterChain 객체 반환
        return http.build();
    }
}