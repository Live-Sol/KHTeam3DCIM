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

import java.util.List;

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
        return "member/findMembersUser"; // findMembersUser.html
    }


    // 전체 회원 조회 (관리자용)
    @GetMapping("/admin")
    public String getAllMembersAdmin(Model model) {
        List<MemberAdminResponse> members = memberService.findAllMembersAdmin();
        model.addAttribute("members", members);  // 회원 목록을 모델에 추가
        return "member/findMembersAdmin";  // "member/findMembersAdmin" 템플릿을 반환
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
    @PostMapping("/login")
    public String login(@RequestParam String memberId,
                        @RequestParam String password,
                        Model model, HttpSession session) {
        boolean success = memberService.login(memberId, password);
        if(!success) {
            model.addAttribute("error", "아이디 또는 비밀번호를 확인해주세요.");
            return "member/login";
        }
        // 로그인 성공 시 세션에 저장
        session.setAttribute("loginId", memberId);
        return "redirect:/";
    }


    // 회원 정보 수정
    @GetMapping("/edit")
    public String editUserForm(HttpSession session, Model model) {

        String loginId = (String) session.getAttribute("loginId");  // 세션에서 로그인 아이디 가져오기

        if (loginId == null) {
            return "redirect:/members/login";

//            return "redirect:/login";   // 로그인 안 했으면 로그인 페이지로
        }

        Member member = memberService.findMember(loginId);
        model.addAttribute("member", member);

        return "member/editMember";
    }

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
