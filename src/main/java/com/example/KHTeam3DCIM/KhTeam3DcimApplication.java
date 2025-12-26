package com.example.KHTeam3DCIM;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Role;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class KhTeam3DcimApplication {

    public static void main(String[] args) {

        SpringApplication.run(KhTeam3DcimApplication.class, args);
    }
    // ⭐️ 초기 데이터 설정을 위한 필드 주입 ⭐️
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 애플리케이션 구동이 완료된 후 (모든 Bean 초기화 완료 후) 실행되는 메서드입니다.
     */
    @EventListener(ApplicationReadyEvent.class) // ⭐️ 이벤트 리스너 사용 ⭐️
    public void initializeDefaultUsers() {

        // 1. 관리자 계정 생성
        if (!memberRepository.existsByMemberId("admin")) {
            memberRepository.save(Member.builder()
                    .memberId("admin")
                    // 비밀번호 암호화는 PasswordEncoder Bean이 준비된 후 실행됩니다..
                    .password(passwordEncoder.encode("1234"))
                    .name("관리자")
                    .email("admin@kh.co.kr")
                    .contact("010-0000-0000")
                    .companyName("KH")
                    .companyPhone("052-123-4567")
                    .role(Role.ADMIN).build());
            System.out.println("⭐ 초기 관리자 계정(admin) 생성 완료 및 암호화 적용");
        }

        // 2. 일반 사용자 계정 생성
        if (!memberRepository.existsByMemberId("user")) {
            memberRepository.save(Member.builder()
                    .memberId("user")
                    .password(passwordEncoder.encode("1234"))
                    .name("김운영")
                    .email("user@kh.co.kr")
                    .contact("010-0000-0000")
                    .companyName("KH")
                    .companyPhone("052-123-4567")
                    .role(Role.USER).build());
            System.out.println("⭐ 초기 사용자 계정(user) 생성 완료 및 암호화 적용");
        }
    }
}