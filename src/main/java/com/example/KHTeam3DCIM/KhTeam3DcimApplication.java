package com.example.KHTeam3DCIM;

import com.example.KHTeam3DCIM.domain.Category; // Category 임포트
import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Role;
import com.example.KHTeam3DCIM.repository.CategoryRepository; // CategoryRepository 임포트
import com.example.KHTeam3DCIM.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays; // 리스트 처리를 위해 추가

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

    @Autowired // [추가] 카테고리 저장을 위해 주입
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 애플리케이션 구동이 완료된 후 (모든 Bean 초기화 완료 후) 실행되는 메서드입니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() { // 메서드 이름을 좀 더 포괄적으로 변경했습니다 (Users -> Data)

        // ==========================================
        // 1. 카테고리 초기 데이터 생성 (요청하신 부분)
        // ==========================================
        if (categoryRepository.count() == 0) { // 데이터가 하나도 없을 때만 실행
            categoryRepository.saveAll(Arrays.asList(
                    new Category("SVR", "Server"),
                    new Category("NET", "Network/Switch"),
                    new Category("STO", "Storage"),
                    new Category("UPS", "UPS/Power")
            ));
            System.out.println("⭐ 초기 카테고리 데이터(SVR, NET, STO, UPS) 생성 완료");
        } else {
            // 혹시 특정 데이터가 빠져있을 수 있으니 개별 체크도 가능 (선택 사항)
            if(!categoryRepository.existsById("SVR")) categoryRepository.save(new Category("SVR", "Server"));
            if(!categoryRepository.existsById("NET")) categoryRepository.save(new Category("NET", "Network/Switch"));
            if(!categoryRepository.existsById("STO")) categoryRepository.save(new Category("STO", "Storage"));
            if(!categoryRepository.existsById("UPS")) categoryRepository.save(new Category("UPS", "UPS/Power"));
        }

        // ==========================================
        // 2. 관리자 계정 생성
        // ==========================================
        if (!memberRepository.existsByMemberId("admin")) {
            memberRepository.save(Member.builder()
                    .memberId("admin")
                    .password(passwordEncoder.encode("1234"))
                    .name("관리자")
                    .email("admin@kh.co.kr")
                    .contact("010-0000-0000")
                    .companyName("KH")
                    .companyPhone("052-123-4567")
                    .role(Role.ADMIN).build());
            System.out.println("⭐ 초기 관리자 계정(admin) 생성 완료 및 암호화 적용");
        }

        // ==========================================
        // 3. 일반 사용자 계정 생성
        // ==========================================
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