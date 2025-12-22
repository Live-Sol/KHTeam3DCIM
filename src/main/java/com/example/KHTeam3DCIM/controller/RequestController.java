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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        return "redirect:/";
    }

    // =======================================
    // 2-1. [고객] 내 신청 이력 조회 (새로 추가)
    // =======================================
    @GetMapping("/my")
    public String myRequestList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인 안 한 사용자는 로그인 페이지로
        }

        String memberId = principal.getName();
        // 서비스의 findMyRequests를 호출하여 내 데이터만 가져옴
        List<Request> myRequests = requestService.findMyRequests(memberId);

        model.addAttribute("requests", myRequests);
        return "request/MyRequestList"; // 이력 조회 페이지 HTML
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
    // 3. [관리자] 대기 중인 신청 목록 확인
    // =======================================
    @GetMapping
    public String requestList(Model model, HttpServletRequest request) {
        // 서비스에서 대기 중인 목록만 가져옴
        List<Request> waitingRequests = requestService.findWaitingRequests();

        model.addAttribute("request", request); // 네비게이션 활성화 등을 위한 서블릿 객체
        model.addAttribute("requests", waitingRequests);
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


}