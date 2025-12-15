package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 전체 회원 조회 (회원용)
    @GetMapping
    public String getAllMembersUser(Model model) {
        List<MemberResponse> members = memberService.findAllMembersUser();
        model.addAttribute("members", members);
        return "member/findMembersUser";
    }
    
    // 특정 회원 조회 (부분 일치 검색)
    @GetMapping("/search/{memberId}")
    public String getMemberById(@PathVariable String memberId, Model model) {
        try {
            List<MemberResponse> members = memberService.findMemberByIdOrLike(memberId); // 부분 일치 검색
            if (members.size() == 1) {
                model.addAttribute("member", members.get(0));  // 정확한 회원 조회
            } else if (members.size() > 1) {
                model.addAttribute("members", members);  // 여러 명이 검색된 경우
            } else {
                model.addAttribute("error", "회원이 존재하지 않습니다.");
            }
            return "member/findMemberById"; // 해당 html로 반환
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());  // 오류 메시지 전달
            return "member/findMemberById";
        }
    }

    // 회원가입 폼 페이지
    @GetMapping("/signup")
    public String signupForm() {
        return "member/signup";  // templates/member/signup.html
    }
    // 회원 등록
    @PostMapping
    public String createMember(@ModelAttribute MemberCreateRequest member, Model model) {
        try {
            MemberResponse response = memberService.addMember(member);
            model.addAttribute("message", "회원가입 성공");
            return "redirect:/members/login";  // 회원가입 성공 후 로그인 페이지로 리다이렉트
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());  // 유효성 검사 오류 메시지 전달
            return "member/signup";  // 오류가 발생하면 회원가입 페이지로 돌아감
        }
    }

    // 로그인 페이지로 이동
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }
    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 삭제 → 로그인 상태 초기화
        return "redirect:/";   // 로그아웃 후 메인페이지로 이동
    }

    // 회원 정보 수정 폼 페이지
    @GetMapping("/edit")
    public String editUserForm(Model model) { // HttpSession 제거
        String loginId = getLoggedInUserId(); // Security Context에서 ID 가져옴

        if (loginId.equals("anonymousUser")) { // 익명 사용자(로그인 안함) 체크
            return "redirect:/members/login";
        }

        Member member = memberService.findMember(loginId);
        model.addAttribute("member", member);

        return "member/editMember"; // 일반 회원 본인 수정 폼
    }
    // 회원 정보 수정 (본인)
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

    // 회원 탈퇴 페이지로 이동
    @GetMapping("/delete")
    public String deleteUserForm(Model model) { // HttpSession 제거
        String loginId = getLoggedInUserId(); // Security Context에서 ID 가져옴

        if (loginId.equals("anonymousUser")) {
            return "redirect:/members/login";
        }

        Member member = memberService.findMember(loginId);
        model.addAttribute("member", member);
        return "member/deleteMember";   // 일반 회원 본인 탈퇴 폼
    }

    // 회원 정보 삭제 (회원 본인)
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(
            @PathVariable String memberId,
            @RequestBody Map<String, String> body) { // HttpSession 제거

        String loggedInId = getLoggedInUserId(); // Security Context에서 ID 가져옴
        String password = body.get("password");

        // 로그인 상태가 아니거나, 토큰의 ID와 Path Variable의 ID가 다르면 권한 없음
        if (loggedInId.equals("anonymousUser") || !loggedInId.equals(memberId)) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없거나 로그인 세션이 만료되었습니다."));
        }

        try {
            memberService.deleteMemberWithPassword(memberId, password);
            SecurityContextHolder.getContext().setAuthentication(null); // 로그아웃 처리
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 인증되지 않았거나 익명 사용자인 경우 (로그인 안함)
            return "anonymousUser";
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        // 기본적으로 Principal 객체가 ID 문자열일 경우
        return authentication.getName();
    }

}
