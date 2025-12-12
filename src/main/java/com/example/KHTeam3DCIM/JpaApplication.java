//package com.example.KHTeam3DCIM;
//
//import com.example.KHTeam3DCIM.domain.Member;
//import com.example.KHTeam3DCIM.domain.Role;
//import com.example.KHTeam3DCIM.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.security.crypto.password.PasswordEncoder; // ⭐️ 추가 import ⭐️
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class JpaApplication implements ApplicationRunner {
//
//    private final MemberRepository memberRepository;
//    // ⭐️ PasswordEncoder 주입 (SecurityConfig에서 Bean으로 등록했음) ⭐️
//    private final PasswordEncoder passwordEncoder;
//
////    private final RackRepository rackRepository; // (주석 처리된 상태 유지)
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//
//        // 🚨 주의: 이 로직은 memberId가 이미 존재하면 오류가 발생합니다.
//        // 따라서, 앱을 처음 시작할 때만 실행되도록 하거나, DB를 비우고 실행해야 합니다.
//
//        // 1. 관리자 비밀번호 암호화 후 저장
//        if (!memberRepository.existsByMemberId("admin")) { // 중복 방지 체크 추가 (선택적)
//            memberRepository.save(Member.builder()
//                    .memberId("admin")
//                    // ⭐️ BCrypt 암호화 적용 ⭐️
//                    .password(passwordEncoder.encode("1234"))
//                    .name("관리자")
//                    .role(Role.ADMIN).build());
//        }
//
//        // 2. 일반 사용자 비밀번호 암호화 후 저장
//        if (!memberRepository.existsByMemberId("user")) { // 중복 방지 체크 추가 (선택적)
//            memberRepository.save(Member.builder()
//                    .memberId("user")
//                    // ⭐️ BCrypt 암호화 적용 ⭐️
//                    .password(passwordEncoder.encode("1234"))
//                    .name("김운영")
//                    .role(Role.USER).build());
//        }
//
////        rackRepository.save(Rack.builder()
////                        .rackName("A-01")
////                        .totalUnit((long) 42)
////                        .locationDesc("3층 메인 전산실 입구 좌측").build());
//    }
//}