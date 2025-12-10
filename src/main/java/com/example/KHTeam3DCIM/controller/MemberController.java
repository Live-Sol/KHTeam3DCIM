package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 전체 회원 조회
    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.findAllMembers();
    }

    // 회원 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMember(@PathVariable String memberId) {
        try{
            Member member = memberService.findByMemberIdOrThrow(memberId);
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

//    나중에 DTO 생성시 아래의 스크립트 추가 후
//    public class MemberResponse {
//        private Member member;
//        private String message;
//
//        // 생성자, getter, setter
//        public MemberResponse(Member member, String message) {
//            this.member = member;
//            this.message = message;
//        }
//    }

//    회원 조회를 아래와 같이 하면 조회 실패시 메시지와 함께 null 값 리턴
//    (현재는 DB에 없는 데이터 조회시 메세지 없이  null 값 리턴 하는 상태)
//    @GetMapping("/{memberId}")
//    public MemberResponse getMember(@PathVariable String memberId) {
//        Optional<Member> member = memberService.findById(memberId);
//        if (member.isEmpty()) {
//            return new MemberResponse(null, "조회 불가");
//        }
//        return new MemberResponse(member.get(), null); // 조회 성공 메시지는 생략
//    }

    // 3. 회원 추가
    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberService.addMember(member);
    }

    // 4. 회원 정보 수정
    @PatchMapping("/{memberId}")
    public ResponseEntity<Member> patchMember(@PathVariable String memberId, @RequestBody Member patch) {
        Member updated = memberService.updateMember(memberId, patch);
        if(updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // 5. 회원 정보 삭제
    @DeleteMapping("/{memberId}")
    public void deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
    }



}
