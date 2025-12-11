package com.example.KHTeam3DCIM;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.domain.Role;



@Component
@Slf4j
@RequiredArgsConstructor
public class JpaApplication implements ApplicationRunner {

    // 실행시 member 2명, rack 1개 자동 insert 되는 기능 (충돌 발생시 생략 가능)
    private final MemberRepository memberRepository;
    private final RackRepository rackRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        memberRepository.save(Member.builder()
                .memberId("user")
                .password("1234")
                .name("김운영")
                .role(Role.USER).build());
        memberRepository.save(Member.builder()
                .memberId("admin")
                .password("1234")
                .name("관리자")
                .role(Role.ADMIN).build());
//        rackRepository.save(Rack.builder()
//                        .rackName("A-01")
//                        .totalUnit((long) 42)
//                        .locationDesc("3층 메인 전산실 입구 좌측").build());
    }
}
