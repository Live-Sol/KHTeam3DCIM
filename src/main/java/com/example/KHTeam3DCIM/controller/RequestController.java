// 파일의 역할 : 신청서(Request) 작성 및 관리와 관련된 요청을 처리하는 컨트롤러 클래스
package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.dto.Request.RequestDTO;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.MemberService;
import com.example.KHTeam3DCIM.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/requests") // 공통 경로 추출
public class RequestController {

    private final RequestService requestService;
    private final CategoryService categoryService;
    private final MemberService memberService;

    // =======================================
    // 1. [고객] 입고 신청서 작성 화면
    // =======================================
    @GetMapping("/new")
    public String requestForm(Model model, Principal principal) {
        RequestDTO dto = new RequestDTO();
        dto.setHeightUnit(2); // 기본 높이 2U 설정

        // 로그인 상태라면 회원 정보를 DTO에 미리 채움 (자동완성)
        if (principal != null) {
            try {
                Member member = memberService.findMember(principal.getName());
                dto.setCompanyName(member.getCompanyName());
                dto.setCompanyPhone(member.getCompanyPhone());
                dto.setUserName(member.getName());
                dto.setContact(member.getContact());
            } catch (Exception e) {
                // 회원 정보 로드 실패 시 로그만 남기고 빈 폼 제공
            }
        }

        model.addAttribute("requestDTO", dto);
        model.addAttribute("categories", categoryService.findAllCategories());
        return "request/RequestForm";
    }

    // =======================================
    // 2. [고객] 신청서 제출 처리 (수정됨)
    // =======================================
    @PostMapping("/new")
    public String createRequest(@Valid @ModelAttribute("requestDTO") RequestDTO requestDTO,
                                BindingResult result,
                                Principal principal, // 1. Principal 파라미터 추가
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("hasErrors", true);
            return "request/RequestForm";
        }

        // 2. 로그인한 사용자의 ID를 DTO에 저장 (누가 신청했는지 기록)
        if (principal != null) {
            requestDTO.setMemberId(principal.getName());
        }

        requestService.saveRequest(requestDTO);
        return "redirect:/requests/my";
    }

    // =======================================
    // 2-1. [고객] 내 신청 이력 조회 (새로 추가)
    // =======================================
    @GetMapping("/my")
    public String myRequestList(
            @PageableDefault(size = 5, sort = "reqDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String sort,    // 추가
            @RequestParam(required = false) String sortDir, // 추가
            @RequestParam(required = false) String keyword, // 추가
            Model model,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String memberId = principal.getName();

        // 서비스 메서드에 memberId와 pageable을 함께 전달하도록 수정해야 함
        Page<Request> requestPage = requestService.findMyRequestsPaged(memberId, keyword, sort, sortDir, pageable);

        model.addAttribute("requests", requestPage.getContent()); // 테이블에 뿌릴 리스트
        model.addAttribute("page", requestPage);                 // 페이징 UI를 위한 정보
        model.addAttribute("sort", sort != null ? sort : "reqDate"); // 기본값 설정
        model.addAttribute("sortDir", sortDir != null ? sortDir : "desc");
        model.addAttribute("keyword", keyword);

        return "request/MyRequestList";
    }

    @GetMapping("/members/{memberId}")
    public String findMemberByIdFromRequest(@PathVariable String memberId, Model model) {
        try {
            Member member = memberService.findByMemberId(memberId);
            model.addAttribute("member", member);
        } catch (IllegalArgumentException e) {
            model.addAttribute("memberNotFound", true);
        }

        // 목록으로 버튼은 항상 요청 목록으로
        model.addAttribute("returnUrl", "/requests");

        return "member/memberDetail"; // 요청용 상세 페이지
    }
    // =======================================
    // 2-2. [고객] 내 신청 이력 삭제/숨김 처리
    // =======================================
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> removeRequest(@PathVariable Long id) {
        try {
            requestService.processRemoveOrHide(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 처리 중 오류 발생");
        }
    }
    // 숨김 내역 페이지 이동
    @GetMapping("/my-hidden-requests")
    public String myHiddenRequestList(
            @PageableDefault(size = 5, sort = "reqDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String keyword,
            Model model,
            Principal principal) {

        if (principal == null) return "redirect:/login";

        String memberId = principal.getName();

        // 서비스 호출 (인자 5개)
        Page<Request> requestPage = requestService.findHiddenRequestsPaged(memberId, keyword, sort, sortDir, pageable);

        model.addAttribute("requests", requestPage.getContent());
        model.addAttribute("page", requestPage);
        model.addAttribute("sort", sort != null ? sort : "reqDate");
        model.addAttribute("sortDir", sortDir != null ? sortDir : "desc");
        model.addAttribute("keyword", keyword);

        return "request/myHiddenList"; // 해당 HTML 파일명
    }


    // 숨김 해제 처리 (복구)
    @PostMapping("/restore/{id}")
    @ResponseBody
    public ResponseEntity<String> restoreRequest(@PathVariable Long id, Principal principal) {
        try {
            requestService.restoreRequest(id, principal.getName());
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }


    // =======================================
    // 3. [관리자] 대기 중인 신청 목록 확인
    // =======================================
    @GetMapping
    public String requestList(Model model, HttpServletRequest request,
                              @RequestParam(required = false) String keyword,        // 검색어
                              @RequestParam(required = false, defaultValue = "ALL") String emsStatus) { // 필터

        // 서비스 호출 시 파라미터 전달
        List<Request> waitingRequests = requestService.findWaitingRequests(keyword, emsStatus);

        model.addAttribute("request", request);
        model.addAttribute("requests", waitingRequests);

        // ⭐ 검색 조건 유지 (화면의 input value에 다시 넣어주기 위함)
        model.addAttribute("paramKeyword", keyword);
        model.addAttribute("paramEmsStatus", emsStatus);

        return "request/RequestList";
    }

    // =======================================
    // 4. [관리자] 신청 반려 처리
    // =======================================
    @GetMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id) {
        // 직접 Repository를 부르지 않고 서비스를 통해 상태 변경
        requestService.updateStatus(id, "REJECTED");
        return "redirect:/requests";
    }

    // [추가] 반려 사유 입력 화면
    @GetMapping("/{id}/reject/form")
    public String rejectForm(@PathVariable Long id, Model model) {
        model.addAttribute("request", requestService.findById(id));
        return "request/RejectForm";
    }
    // [추가] 반려 사유 포함 반려 처리
    @PostMapping("/{id}/reject")
    public String rejectRequestWithReason(
            @PathVariable Long id,
            @RequestParam(required = false) String rejectReason) {

        requestService.rejectRequest(id, rejectReason);
        return "redirect:/requests";
    }

    // =======================================
    // 5. [관리자] 입고 승인 및 장비 등록 처리 (추가)
    // =======================================
    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam Long rackId,
                                 @RequestParam Integer startUnit,
                                 RedirectAttributes redirectAttributes) { // RedirectAttributes 추가
        try {
            requestService.approveRequest(id, rackId, startUnit);
            redirectAttributes.addFlashAttribute("successMessage", "성공적으로 승인되었습니다.");
            return "redirect:/requests";
        } catch (IllegalStateException e) {
            // 탈퇴한 회원 등 비즈니스 로직 상의 오류 처리

            // 서비스에서 던진 "신청자 정보 없음" 예외를 여기서 가로채서
            // 500 에러 페이지 대신 목록 페이지에 경고 메시지(FlashAttribute)를 들고 돌아감
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/requests";
        } catch (Exception e) {
            // 기타 예상치 못한 오류
            redirectAttributes.addFlashAttribute("errorMessage", "승인 중 알 수 없는 오류가 발생했습니다.");
            return "redirect:/requests";
        }
    }
    //

}