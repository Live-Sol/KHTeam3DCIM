    package com.example.KHTeam3DCIM.controller;

    import com.example.KHTeam3DCIM.domain.AuditLog;
    import com.example.KHTeam3DCIM.domain.Member;
    import com.example.KHTeam3DCIM.dto.admin.MemberAdminResponse;
    import com.example.KHTeam3DCIM.dto.admin.MemberAdminUpdateRequest;
    import com.example.KHTeam3DCIM.service.AdminService;
    import com.example.KHTeam3DCIM.service.AuditLogService;
    import com.example.KHTeam3DCIM.service.MemberService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor; // ⭐️ Lombok의 RequiredArgsConstructor 사용 ⭐️
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

            // 🚨 헤더용 모델 속성 제거: header.html이 sec:authorize로 정보를 직접 가져감..

            // --- 4. 통계/로그 데이터 추가 (데이터 처리만 남김) ---
            int pendingRequestCount = auditLogService.getPendingRequestCount();
            int totalDeviceCount = auditLogService.getTotalDeviceCount();
            int totalMemberCount = auditLogService.getTotalMemberCount();
            int totalRackCount = auditLogService.getTotalRackCount();

            int logLimit = 5;
            List<AuditLog> recentLogs = auditLogService.getRecentActivityLogs(logLimit);

            model.addAttribute("pageTitle", "관리자 페이지");
            model.addAttribute("pendingRequestCount", pendingRequestCount);
            model.addAttribute("totalDeviceCount", totalDeviceCount);
            model.addAttribute("totalMemberCount", totalMemberCount);
            model.addAttribute("totalRackCount", totalRackCount);
            model.addAttribute("recentLogs", recentLogs);
            model.addAttribute("request", request);

            return "admin"; // templates/admin.html
        }

        /**
         * (1-1) 관리자: 전체 회원 조회 목록 페이지 제공
         *  - 관리자 권한을 가진 사용자가
         *    시스템에 등록된 모든 회원 목록을 조회하기 위한 페이지를 반환한다.
         *  - 회원 정보는 Service 계층에서 DTO 형태로 조회하여
         *    View(Thymeleaf)로 전달한다.
         *  - 또한 관리자 대시보드에 표시할
         *    최근 감사 로그(Audit Log) 정보도 함께 조회한다.
         *  URL: /admin/members
         *  Method: GET
         */
        @GetMapping("/members")
        public String findAllMembersAdmin(Model model) {

            // 1️⃣ Service 계층을 통해 전체 회원 정보를 조회
            //    - Member 엔티티가 아닌
            //    - 관리자 화면에 필요한 정보만 담은 DTO(MemberAdminResponse) 목록
            List<MemberAdminResponse> members =
                    adminService.findAllMembersAdmin();

            // 2️⃣ 조회한 회원 목록을 "members"라는 이름으로 Model에 저장
            //    → Thymeleaf 템플릿에서 ${members}로 접근 가능
            model.addAttribute("members", members);

            // 3️⃣ 최근 감사 로그(Audit Log) 조회
            //    - 관리자 화면에서 최근 시스템 활동을 확인하기 위함
            //    - 최근 5건만 조회
            List<AuditLog> recentLogs =
                    auditLogService.findRecentLogs(5);

            // 4️⃣ 감사 로그 목록을 Model에 저장
            //    → 템플릿에서 ${recentLogs}로 접근 가능
            model.addAttribute("recentLogs", recentLogs);

            // 5️⃣ 관리자 회원 목록 화면 반환
            //    - templates/admin/findMembersAdmin.html
            return "admin/findMembersAdmin";
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

            return "admin/editMemberAdmin"; // 관리자 정보 수정 폼
        }
        // (2-2) 회원 정보 수정 폼 제공 (GET) - 변경 없음
        @GetMapping("/members-edit/{memberId}")
        public String editMemberAdminForm(@PathVariable String memberId, Model model) {
            try {
                Member member = memberService.findMember(memberId);

                // 1. DB에서 가져온 데이터를 기반으로 기본 DTO 객체 생성
                MemberAdminUpdateRequest currentInfo = MemberAdminUpdateRequest.builder()
                        .name(member.getName())
                        .email(member.getEmail())

                        // ⭐️ 수정 1: String 필드가 null일 경우 빈 문자열로 초기화 ⭐️
                        .contact(member.getContact() != null ? member.getContact() : "")

                        // ⭐️ 수정 2: Role이 null일 경우 기본값 (예: USER)으로 초기화 ⭐️
                        // (Role은 필수이므로 null이 되면 안 되지만, 방어적 코드 추가)
                        .role(member.getRole() != null ? member.getRole() : com.example.KHTeam3DCIM.domain.Role.USER)

                        .companyName(member.getCompanyName())

                        // ⭐️ 수정 3: String 필드가 null일 경우 빈 문자열로 초기화 ⭐️
                        .companyPhone(member.getCompanyPhone() != null ? member.getCompanyPhone() : "")

                        .build();

                // 2. Model에 DTO를 추가합니다.
                //    Flash Attribute가 있다면 Spring이 이전에 추가했으므로 덮어쓰지 않습니다.
                //    (Spring의 addAttribute 동작 방식에 의존)
                // ⭐️ 가장 안전한 방법은 아래와 같이 Model Attribute를 추가하는 것입니다. ⭐️

                // 만약 Flash Attribute (유효성 검사 실패 DTO)가 Model에 없다면, currentInfo를 사용합니다.
                if (model.getAttribute("memberAdminUpdateRequest") == null) {
                    model.addAttribute("memberAdminUpdateRequest", currentInfo);
                }

                model.addAttribute("targetMemberId", memberId);
                return "admin/editMemberAdmin";
            } catch (RuntimeException e) {
                return "redirect:/admin/members";
            }
        }

        // ⭐️ 2. 회원 정보 수정 처리 (POST로 변경) ⭐️
        @PostMapping("/members-edit/{memberId}")
        public String updateMemberAdmin(@PathVariable String memberId,
                                        @ModelAttribute @Valid MemberAdminUpdateRequest memberAdminUpdateRequest,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        @AuthenticationPrincipal UserDetails userDetails) {

            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: 입력 값을 확인해 주세요.");
                redirectAttributes.addFlashAttribute("memberAdminUpdateRequest", memberAdminUpdateRequest);
                redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "memberAdminUpdateRequest", bindingResult);

                // POST 요청 후 GET 요청으로 리다이렉트하여 오류 내용을 Flash Attribute로 전달
                return "redirect:/admin/members-edit/" + memberId;
            }
            // 2. 관리자 ID 추출
            // UserDetails의 getUsername()은 일반적으로 인증 주체(여기서는 관리자 ID)를 반환합니다.
            String adminActorId = userDetails.getUsername();
            try {
                // ⭐️ 3. 서비스 호출: 수정 대상 ID, DTO, 관리자 ID(Actor ID) 전달 ⭐️
                adminService.updateMemberByAdmin(memberId, memberAdminUpdateRequest, adminActorId);

                // 4. 성공 리다이렉트
                redirectAttributes.addFlashAttribute("updateSuccess", true);
                redirectAttributes.addFlashAttribute("successMessage", "[" + memberId + "] 회원의 정보가 성공적으로 수정되었습니다.");

                return "redirect:/admin/members";
            } catch (RuntimeException e) {
                // 5. 실패 리다이렉트
                redirectAttributes.addFlashAttribute("errorMessage", "수정 실패: " + e.getMessage());
                return "redirect:/admin/members-edit/" + memberId;
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