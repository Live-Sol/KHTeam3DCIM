package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.entity.AuditLog;
import com.example.KHTeam3DCIM.service.AuditLogService;
import com.example.KHTeam3DCIM.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    private final AuditLogService auditLogService;

    // 전체 회원 조회 (회원용)
    @GetMapping
    public String getAllMembersUser(Model model) {
        List<MemberResponse> members = memberService.findAllMembersUser();
        model.addAttribute("members", members);
        return "member/findMembersUser"; // findMembersUser.html
    }

    @GetMapping("/admin")
    public String getAllMembersAdmin(Model model) {

        // 1. 회원 목록 조회 로직 (기존)
        List<MemberAdminResponse> members = memberService.findAllMembersAdmin();
        model.addAttribute("members", members);

        // ⭐️ 2. 최근 로그 조회 및 모델 추가 ⭐️
        // AuditLogService가 최근 5개 로그를 반환하는 findRecentLogs 메서드를 가지고 있다고 가정
        List<AuditLog> recentLogs = auditLogService.findRecentLogs(5);
        model.addAttribute("recentLogs", recentLogs); // ⭐️ 모델 키: "recentLogs"

        return "member/findMembersAdmin";
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

        return "member/editMember";
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
    // 회원 정보 수정 (관리자)
    @PatchMapping("/admin/edit/{memberId}")
    public String updateMemberAdmin(@PathVariable String memberId,
                                    @ModelAttribute @Valid MemberAdminUpdateRequest updateRequest,
                                    BindingResult bindingResult, // 유효성 검사 결과
                                    RedirectAttributes redirectAttributes) {

        // 1. 유효성 검사 실패 시 처리
        if (bindingResult.hasErrors()) {
            // 검증 오류 메시지를 추출하여 사용자에게 전달
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

            redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: " + errorMessage);

            // 오류 발생 시 다시 수정 폼으로 리다이렉트 (데이터는 Service에서 새로 가져옴)
            return "redirect:/members/admin/edit/" + memberId;
        }

        // 2. 유효성 검사 통과 시 비즈니스 로직 실행
        try {
            String currentAdminId = getLoggedInUserId();

            // Service 호출하여 수정 처리
            memberService.updateMemberByAdmin(memberId, updateRequest, currentAdminId);

            redirectAttributes.addFlashAttribute("successMessage",
                    memberId + " 회원의 정보가 성공적으로 수정되었습니다.");

            // 성공 시 수정 폼으로 리다이렉트
            return "redirect:/members/admin/edit/" + memberId;

        } catch (RuntimeException e) {
            // Service 로직 실행 중 발생하는 예외 처리 (예: 회원이 존재하지 않음)
            redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: " + e.getMessage());
            return "redirect:/members/admin/edit/" + memberId;
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
        return "member/deleteMember";
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
            // ⭐️ Security 세션 종료 처리 (로그아웃 처리) ⭐️
            SecurityContextHolder.getContext().setAuthentication(null);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 회원 삭제 요청 처리
    @DeleteMapping("/admin/delete/{memberId}")
    public String deleteMember(@PathVariable String memberId, RedirectAttributes redirectAttributes) {
        // Spring Security에서 현재 관리자 ID를 확보
        String currentAdminId = getLoggedInUserId();
        try {
            // 1. Service 호출: 회원 삭제 처리
            memberService.deleteMember(memberId, currentAdminId);
            // 2. 성공 메시지 설정
            redirectAttributes.addFlashAttribute("deleteMessage", "회원 ID: " + memberId + "가 성공적으로 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("deleteSuccess", true);

        } catch (RuntimeException e) {
            // 3. 실패 메시지 설정 (예: 회원이 존재하지 않을 경우)
            redirectAttributes.addFlashAttribute("deleteMessage", "삭제 실패: " + e.getMessage());
            redirectAttributes.addFlashAttribute("deleteSuccess", false);
        }

        // 4. 회원 목록 페이지로 리다이렉트
        return "redirect:/members/admin";
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
