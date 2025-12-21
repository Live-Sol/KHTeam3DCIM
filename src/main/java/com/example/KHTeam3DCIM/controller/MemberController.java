package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.security.CustomUserDetails;
import com.example.KHTeam3DCIM.service.MailService;
import com.example.KHTeam3DCIM.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MailService mailService;

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
        return "member/signup";
    }

    // 회원 등록
    @PostMapping
    public String createMember(@ModelAttribute MemberCreateRequest member, Model model) {
        try {
            memberService.addMember(member);
            model.addAttribute("message", "회원가입 성공");
            return "redirect:/members/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "member/signup";
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
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        return "member/login";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // GET: 회원 수정 폼 보기
    @GetMapping("/edit")
    public String editMemberForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String memberId = userDetails.getUsername();
        Member member = memberService.findMember(memberId);

        // 현재 정보 채우기
        MemberUpdateRequest currentInfo = new MemberUpdateRequest();
        currentInfo.setEmail(member.getEmail());
        currentInfo.setContact(member.getContact());
        currentInfo.setCompanyName(member.getCompanyName());
        currentInfo.setCompanyPhone(member.getCompanyPhone());

        model.addAttribute("memberId", memberId);
        model.addAttribute("memberUpdateByUserRequest", currentInfo);
        model.addAttribute("memberName", member.getName());
        model.addAttribute("memberRole", member.getRole());
        model.addAttribute("profileImage", member.getProfileImage());

        return "member/editMember";
    }

    // POST: 회원 수정 처리 (이미지 업로드 포함)
    @PostMapping("/edit")
    public String updateMemberUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("memberUpdateByUserRequest") MemberUpdateRequest request,
            BindingResult bindingResult,
            Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", userDetails.getUsername());
            Member member = memberService.findMember(userDetails.getUsername());
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberRole", member.getRole());
            model.addAttribute("profileImage", member.getProfileImage());
            return "member/editMember";
        }

        try {
            String memberId = userDetails.getUsername();
            memberService.updateMember(memberId, request);
            updateSecurityContext(memberId);

            redirectAttributes.addFlashAttribute("updateSuccess", true);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/";

        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "수정 실패: " + e.getMessage());
        }

        Member member = memberService.findMember(userDetails.getUsername());
        model.addAttribute("memberName", member.getName());
        model.addAttribute("memberRole", member.getRole());
        model.addAttribute("memberId", userDetails.getUsername());
        model.addAttribute("profileImage", member.getProfileImage());
        return "member/editMember";
    }

    private void updateSecurityContext(String memberId) {
        Member updatedMember = memberService.findMember(memberId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails newPrincipal = new CustomUserDetails(updatedMember, auth.getAuthorities());
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal, auth.getCredentials(), newPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @GetMapping("/delete")
    public String deleteUserForm(Model model) {
        String loginId = getLoggedInUserId();
        if (loginId.equals("anonymousUser")) {
            return "redirect:/members/login";
        }
        Member member = memberService.findMember(loginId);
        model.addAttribute("member", member);
        return "member/deleteMember";
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable String memberId, @RequestBody Map<String, String> body) {
        String loggedInId = getLoggedInUserId();
        String password = body.get("password");

        if (loggedInId.equals("anonymousUser") || !loggedInId.equals(memberId)) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없거나 로그인 세션이 만료되었습니다."));
        }

        try {
            memberService.deleteMemberWithPassword(memberId, password);
            SecurityContextHolder.getContext().setAuthentication(null);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "anonymousUser";
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName();
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "member/forgot_password";
    }

    // ⭐️ 1. 인증번호 발송 API (안전한 예외 처리)
    @PostMapping("/send-verification-code")
    @ResponseBody
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request, HttpSession session) {
        String memberId = request.get("memberId");
        String email = request.get("email");

        try {
            // 회원 조회 시 회원이 없으면 RuntimeException 발생
            Member member = memberService.findMember(memberId);

            if (!member.getEmail().equals(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "입력하신 아이디의 이메일 정보와 일치하지 않습니다."));
            }

            // 인증번호 생성 및 발송
            String code = String.valueOf(new Random().nextInt(900000) + 100000);
            mailService.sendEmail(email, code);

            session.setAttribute("verificationCode", code);
            session.setAttribute("verifiedMemberId", memberId);

            return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다. 이메일을 확인해주세요."));

        } catch (RuntimeException e) {
            // 회원 없음 에러 처리
            return ResponseEntity.badRequest().body(Map.of("message", "존재하지 않는 회원 아이디입니다."));
        } catch (Exception e) {
            // 메일 서버 오류 등
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    // 2. 인증번호 확인 API
    @PostMapping("/verify-code")
    @ResponseBody
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request, HttpSession session) {
        String inputCode = request.get("code");
        String sessionCode = (String) session.getAttribute("verificationCode");

        if (sessionCode != null && sessionCode.equals(inputCode)) {
            session.setAttribute("isVerified", true);
            return ResponseEntity.ok(Map.of("message", "인증되었습니다. 비밀번호를 변경하세요."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
        }
    }

    // 3. 비밀번호 재설정
    @PostMapping("/forgot-password")
    public String resetPassword(@ModelAttribute MemberPasswordResetRequest request,
                                HttpSession session, Model model) {

        Boolean isVerified = (Boolean) session.getAttribute("isVerified");
        String verifiedMemberId = (String) session.getAttribute("verifiedMemberId");

        if (isVerified == null || !isVerified || !request.getMemberId().equals(verifiedMemberId)) {
            model.addAttribute("error", "이메일 인증을 먼저 진행해주세요.");
            return "member/forgot_password";
        }

        try {
            memberService.resetPassword(request);

            session.removeAttribute("verificationCode");
            session.removeAttribute("isVerified");
            session.removeAttribute("verifiedMemberId");

            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 로그인해주세요.");
            return "member/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "member/forgot_password";
        }
    }
}