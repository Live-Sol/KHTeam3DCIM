package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.dto.Member.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.security.CustomUserDetails; // Import 추가
import com.example.KHTeam3DCIM.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import 추가
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

        // 프로필 이미지 경로 추가 (없으면 null)
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

        // 1. 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", userDetails.getUsername());
            Member member = memberService.findMember(userDetails.getUsername());
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberRole", member.getRole());
            model.addAttribute("profileImage", member.getProfileImage()); // 에러 시에도 기존 이미지 유지
            return "member/editMember";
        }

        try {
            // 2. 서비스 호출 (DB 업데이트 & 파일 저장)
            String memberId = userDetails.getUsername();
            memberService.updateMember(memberId, request);

            // ⭐️ [중요] 세션 정보(SecurityContext) 강제 갱신 ⭐️
            // DB 내용은 바뀌었지만, 현재 로그인된 정보(Authentication)는 옛날 상태입니다.
            // 헤더의 이미지가 바로 바뀌려면 여기서 최신 정보를 다시 불러와 세션에 덮어씌워야 합니다.
            updateSecurityContext(memberId);

            redirectAttributes.addFlashAttribute("updateSuccess", true);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/";

        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "수정 실패: " + e.getMessage());
        }

        // 오류 발생 시 데이터 복구하여 폼 다시 보여주기
        Member member = memberService.findMember(userDetails.getUsername());
        model.addAttribute("memberName", member.getName());
        model.addAttribute("memberRole", member.getRole());
        model.addAttribute("memberId", userDetails.getUsername());
        model.addAttribute("profileImage", member.getProfileImage());
        return "member/editMember";
    }

    // ⭐️ 세션 갱신을 위한 헬퍼 메서드 ⭐️
    private void updateSecurityContext(String memberId) {
        // 1. 최신 회원 정보 DB에서 가져오기
        Member updatedMember = memberService.findMember(memberId);

        // 2. 현재 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 3. 새 UserDetails 객체 생성 (여기서 갱신된 profileImage가 들어감)
        CustomUserDetails newPrincipal = new CustomUserDetails(updatedMember, auth.getAuthorities());

        // 4. 새로운 인증 토큰 생성 (기존 자격증명과 권한 유지)
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal, auth.getCredentials(), newPrincipal.getAuthorities());

        // 5. 시큐리티 컨텍스트에 새 토큰 설정 (즉시 반영)
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // 회원 탈퇴 페이지
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

    // 회원 정보 삭제
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

    @PostMapping("/forgot-password")
    public String resetPassword(@ModelAttribute MemberPasswordResetRequest request, Model model) {
        try {
            if (request.getNewPassword() == null || !request.getNewPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{5,20}$")) {
                throw new IllegalArgumentException("비밀번호는 영문자와 숫자를 포함하여 5~20자여야 합니다.");
            }
            memberService.resetPassword(request);
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 로그인해주세요.");
            return "member/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "member/forgot_password";
        }
    }
}