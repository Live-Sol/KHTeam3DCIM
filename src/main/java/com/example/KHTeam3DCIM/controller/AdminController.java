    package com.example.KHTeam3DCIM.controller;

    import com.example.KHTeam3DCIM.domain.AuditLog;
    import com.example.KHTeam3DCIM.domain.Member;
    import com.example.KHTeam3DCIM.dto.Member.MemberAdminResponse;
    import com.example.KHTeam3DCIM.dto.admin.MemberAdminUpdateRequest;
    import com.example.KHTeam3DCIM.service.AdminService;
    import com.example.KHTeam3DCIM.service.AuditLogService;
    import com.example.KHTeam3DCIM.service.MemberService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor; // ⭐️ Lombok의 RequiredArgsConstructor 사용 ⭐️
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;
    import jakarta.servlet.http.HttpServletRequest;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.util.List;

    @Controller
    @RequestMapping("/admin")
    @RequiredArgsConstructor // ⭐️ final 필드 주입을 위한 어노테이션 추가 ⭐️
    public class AdminController {

        // final 필드로 선언하고 @RequiredArgsConstructor를 사용하면 생성자가 대체됨
        private final AuditLogService auditLogService;
        private final AdminService adminService;
        private final MemberService memberService;

        @GetMapping
        public String adminDashboard(Model model, HttpServletRequest request) { // ⭐️ HttpSession 제거 ⭐️

            // 🚨 권한 체크 로직 제거: Spring SecurityConfig가 이미 hasRole('ADMIN')을 검사했음.

            // 🚨 헤더용 모델 속성 제거: header.html이 sec:authorize로 정보를 직접 가져감.

            // --- 4. 통계/로그 데이터 추가 (데이터 처리만 남김) ---
            int pendingRequestCount = auditLogService.getPendingRequestCount();
            int totalDeviceCount = auditLogService.getTotalDeviceCount();
            int totalMemberCount = auditLogService.getTotalMemberCount();
            int logLimit = 5;
            List<AuditLog> recentLogs = auditLogService.getRecentActivityLogs(logLimit);

            model.addAttribute("pageTitle", "대시보드 홈");
            model.addAttribute("pendingRequestCount", pendingRequestCount);
            model.addAttribute("totalDeviceCount", totalDeviceCount);
            model.addAttribute("totalMemberCount", totalMemberCount);
            model.addAttribute("recentLogs", recentLogs);
            model.addAttribute("request", request);

            return "admin"; // templates/admin.html
        }

        // (1) 전체 회원 조회 (관리자용)
        // URL: /admin/members
        @GetMapping("/members")
        public String getAllMembersAdmin(Model model) {
            List<MemberAdminResponse> members = adminService.findAllMembersAdmin();
            model.addAttribute("members", members);
            // 최근 로그 조회
            List<AuditLog> recentLogs = auditLogService.findRecentLogs(5);
            model.addAttribute("recentLogs", recentLogs);
            return "member/findMembersAdmin";
        }

        // ⭐️ 관리자 회원 정보 수정 기능 (/admin/members-edit/**) ⭐️
        // (2-1) 관리자 정보 수정 폼 제공 (GET)
        @GetMapping("/edit")
        public String editUserForm(Model model) { // HttpSession 제거
            String loginId = getLoggedInUserId(); // Security Context에서 ID 가져옴

            if (loginId.equals("anonymousUser")) { // 익명 사용자(로그인 안함) 체크
                return "redirect:/members/login";
            }

            Member member = adminService.findMember(loginId);
            model.addAttribute("member", member);

            return "admin/editAdmin"; // 관리자 정보 수정 폼
        }
        // (2-2) 회원 정보 수정 폼 제공 (GET)
        @GetMapping("/members-edit/{memberId}") // ️ URL 경로 변경
        public String editMemberAdminForm(@PathVariable String memberId, Model model) {
            try {
                Member member = adminService.findMember(memberId);
                model.addAttribute("member", member);

                // ⭐️ 오류 해결 지점 ⭐️
                // 유효성 검사 실패 시 Flash Attribute에 DTO가 없을 때만 초기화
                if (!model.containsAttribute("memberAdminUpdateRequest")) {
                    model.addAttribute("memberAdminUpdateRequest", MemberAdminUpdateRequest.builder()
                            .name(member.getName())
                            .email(member.getEmail())
                            .contact(member.getContact())
                            .role(member.getRole())
                            .build());
                }
                return "member/editMemberAdmin"; // templates/member/editMemberAdmin.html
            } catch (RuntimeException e) {
                model.addAttribute("errorMessage", "오류: " + e.getMessage());
                return "redirect:/admin/members"; // 목록 페이지로 리다이렉트
            }
        }

        // (2-2) 회원 정보 수정 처리 (PATCH)
        // 최종 URL: /admin/members-edit/{memberId}
        @PatchMapping("/members-edit/{memberId}") // ⭐️ URL 경로 변경 ⭐️
        public String updateMemberAdmin(@PathVariable String memberId,
                                        @ModelAttribute @Valid MemberAdminUpdateRequest memberAdminUpdateRequest,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
            // BindingResult는 @ModelAttribute 바로 다음에 와야 하므로, 매개변수 이름을 변경하면 BindingResult도 변경되어야 합니다.
            String bindingResultKey = "org.springframework.validation.BindingResult.memberAdminUpdateRequest"; // ⭐️ 키 변경 ⭐️

            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: 입력 값을 확인해 주세요.");
                redirectAttributes.addFlashAttribute("memberAdminUpdateRequest", memberAdminUpdateRequest); // ⭐️ 모델 속성 이름 변경 ⭐️
                redirectAttributes.addFlashAttribute(bindingResultKey, bindingResult); // ⭐️ 키 변경 ⭐️

                return "redirect:/admin/members-edit/" + memberId; // 리다이렉트 URL 유지
            }

            try {
                // Service 호출 시 변경된 매개변수 이름 사용
                adminService.updateMemberByAdmin(memberId, memberAdminUpdateRequest, getLoggedInUserId());
                redirectAttributes.addFlashAttribute("successMessage", memberId + " 회원의 정보가 성공적으로 수정되었습니다.");

                return "redirect:/admin/members-edit/" + memberId; // 리다이렉트 URL 유지
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: " + e.getMessage());
                return "redirect:/admin/members-edit/" + memberId; // 리다이렉트 URL 유지
            }
        }

        // ⭐️ 관리자 회원 정보 삭제 기능 (/admin/members-delete/**) ⭐️

        // (3) 회원 삭제 요청 처리 (DELETE)
        // 최종 URL: /admin/members-delete/{memberId}
        @DeleteMapping("/members-delete/{memberId}") // ⭐️ URL 경로 변경 ⭐️
        public String deleteMemberAdmin(@PathVariable String memberId, RedirectAttributes redirectAttributes) {

            String currentAdminId = getLoggedInUserId();
            try {
                adminService.deleteMember(memberId, currentAdminId);
                redirectAttributes.addFlashAttribute("deleteMessage", "회원 ID: " + memberId + "가 성공적으로 삭제되었습니다.");
                redirectAttributes.addFlashAttribute("deleteSuccess", true);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("deleteMessage", "삭제 실패: " + e.getMessage());
                redirectAttributes.addFlashAttribute("deleteSuccess", false);
            }
            return "redirect:/admin/members"; // 삭제 후 목록 페이지로 리다이렉트
        }

        // (4) 인증 정보 조회 헬퍼 메서드 (MemberController에서 가져옴)
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
    }