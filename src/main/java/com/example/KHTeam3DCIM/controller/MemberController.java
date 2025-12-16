package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // 전체 회원 조회 (회원용)
    @GetMapping
    public String getAllMembersUser(Model model) {
        List<MemberResponse> members = memberService.findAllMembersUser();
        model.addAttribute("members", members);
        return "member/findMembersUser";
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
    // 아이디 중복체크
    @GetMapping("/check-id")
    @ResponseBody
    public Map<String, Boolean> checkMemberId(@RequestParam String memberId) {
        boolean available = memberService.isMemberIdAvailable(memberId);
        return Map.of("available", available);
    }

    // 로그인 페이지로 이동
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            Model model) {

        // ⭐️ ?error 파라미터가 존재하면 Model에 메시지를 추가 ⭐️
        if (error != null) {
            // 여기서는 간단히 '로그인 실패' 메시지를 사용합니다.
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        return "member/login";
    }
    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 삭제 → 로그인 상태 초기화
        return "redirect:/";   // 로그아웃 후 메인페이지로 이동
    }

    // GET: 회원 수정 폼 보기
    @GetMapping("/edit")
    public String editMemberForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String memberId = userDetails.getUsername();

        // 1. 현재 엔티티 정보 조회
        Member member = memberService.findMember(memberId);

        // 2. DTO에 현재 정보를 담아 폼에 미리 채워줍니다.
        MemberUpdateRequest currentInfo = new MemberUpdateRequest();
        // 비밀번호는 보안상 DTO에 담지 않습니다.
        currentInfo.setEmail(member.getEmail());
        currentInfo.setContact(member.getContact());
        currentInfo.setCompanyName(member.getCompanyName());
        currentInfo.setCompanyPhone(member.getCompanyPhone());

        model.addAttribute("memberId", memberId);
        model.addAttribute("memberUpdateByUserRequest", currentInfo);
        model.addAttribute("memberName", member.getName()); // 이름은 수정 불가하지만 폼에 표시용
        model.addAttribute("memberRole", member.getRole()); // Role은 수정 불가하지만 폼에 표시용

        return "member/editMember";
    }

    // POST: 회원 수정 처리
    @PostMapping("/edit")
    public String updateMemberUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("memberUpdateByUserRequest") MemberUpdateRequest request,
            BindingResult bindingResult,
            Model model, RedirectAttributes redirectAttributes) {

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", userDetails.getUsername());
            // 폼을 다시 렌더링할 때 이름과 역할 정보를 다시 넣어줘야 합니다.
            Member member = memberService.findMember(userDetails.getUsername());
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberRole", member.getRole());
            return "member/editMember";
        }

        try {
            memberService.updateMember(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("updateSuccess", true);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/"; // 수정 완료 후 리다이렉트
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "수정 실패: " + e.getMessage());
            // 오류 발생 시에도 이름과 역할 정보를 다시 넣어줘야 합니다.
            Member member = memberService.findMember(userDetails.getUsername());
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberRole", member.getRole());
            model.addAttribute("memberId", userDetails.getUsername());
            return "member/editMember";
        }
    }


//    // 회원 정보 수정 폼 페이지
//    @GetMapping("/edit")
//    public String editUserForm(Model model) { // HttpSession 제거
//        String loginId = getLoggedInUserId(); // Security Context에서 ID 가져옴
//
//        if (loginId.equals("anonymousUser")) { // 익명 사용자(로그인 안함) 체크
//            return "redirect:/members/login";
//        }
//
//        Member member = memberService.findMember(loginId);
//        model.addAttribute("member", member);
//
//        return "member/editMember"; // 일반 회원 본인 수정 폼
//    }
//    // 회원 정보 수정 (본인)
//    @PatchMapping("/{memberId}")
//    public ResponseEntity<MemberResponse> patchMember(@PathVariable String memberId,
//        @RequestBody MemberUpdateRequest patch) {
//        try{
//            MemberResponse response = memberService.updateMember(memberId, patch);
//            return ResponseEntity.ok(response);
//        }catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

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
