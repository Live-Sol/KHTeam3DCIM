package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.dto.Member.MemberAdminResponse;
import com.example.KHTeam3DCIM.dto.Member.MemberCreateRequest;
import com.example.KHTeam3DCIM.dto.Member.MemberResponse;
import com.example.KHTeam3DCIM.dto.Member.MemberUpdateRequest;
import com.example.KHTeam3DCIM.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 전체 회원 조회(회원용)
    @GetMapping
    public List<MemberResponse> getAllMembersUser() {
        return memberService.findAllMembersUser();
    }
    // 전체 회원 조회 (관리자용)
    @GetMapping("/admin")
    public List<MemberAdminResponse> getAllMembersAdmin() {
        return memberService.findAllMembersAdmin();
    }

    // 특정 회원 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable String memberId) {
        try{
            MemberResponse member = memberService.findMemberById(memberId);
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 회원 추가
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest member) {
        MemberResponse response = memberService.addMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // 회원 정보 수정
    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberResponse> patchMember(@PathVariable String memberId,
        @RequestBody MemberUpdateRequest patch) {
        try{
            MemberResponse response = memberService.updateMember(memberId, patch);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 회원 정보 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }



}
