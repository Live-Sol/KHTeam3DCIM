// 파일의 역할 : 신청서(Request) 작성 및 관리와 관련된 요청을 처리하는 컨트롤러 클래스

package com.example.KHTeam3DCIM.controller;

import java.security.Principal;
import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.dto.Request.RequestDTO;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.MemberService;
import com.example.KHTeam3DCIM.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RequestController {

    private final RequestRepository requestRepository;
    private final CategoryService categoryService;
    private final MemberService memberService;

    // =======================================
    // 1. [고객] 입주 신청서 작성 화면 (수정)
    // =======================================
    @GetMapping("/requests/new")
    public String requestForm(Model model, Principal principal) {
        RequestDTO dto = new RequestDTO();
        dto.setHeightUnit(2);

        if (principal != null) {
            try {
                Member member = memberService.findMember(principal.getName());
                // DTO에 회원 정보를 미리 채워서 넘겨줍니다.
                dto.setCompanyName(member.getCompanyName());
                dto.setCompanyPhone(member.getCompanyPhone());
                dto.setUserName(member.getName());
                dto.setContact(member.getContact());
            } catch (Exception e) { }
        }

        model.addAttribute("requestDTO", dto); // 이제 th:field가 이 값을 읽어 화면에 뿌려줍니다.
        model.addAttribute("categories", categoryService.findAllCategories());
        return "request/RequestForm";
    }
    // =======================================
    // 2. [고객] 신청서 제출 (수정)
    // =======================================
    @PostMapping("/requests/new")
    public String createRequest(@Valid @ModelAttribute("requestDTO") RequestDTO requestDTO,
                                BindingResult result,
                                Model model,
                                Principal principal) {

        // ⭐️ 1. 유효성 검사 에러가 있는지 체크
        if (result.hasErrors()) {
            // 에러가 있다면 입력 폼으로 다시 돌려보냄
            // 이때 카테고리 등 폼 구성에 필요한 데이터를 다시 담아줘야 함
            model.addAttribute("categories", categoryService.findAllCategories());

            if (principal != null) {
                try {
                    Member member = memberService.findMember(principal.getName());
                    model.addAttribute("member", member);
                } catch (Exception e) {}
            }
            // "request/RequestForm"으로 리턴하면 result에 담긴 에러 메시지가 함께 전달됨
            return "request/RequestForm";
        }

        // ⭐️ 2. 검증을 통과했다면 DTO의 toEntity()를 사용하여 저장
        Request request = requestDTO.toEntity();
        requestRepository.save(request);

        return "redirect:/";
    }

//    // =======================================
//    // 1. [고객] 입주 신청서 작성 화면 (수정됨)
//    // =======================================
//    @GetMapping("/requests/new")
//    public String requestForm(Model model, Principal principal,HttpServletRequest request) {
//        // 1. 카테고리 목록 전달
//        model.addAttribute("categories", categoryService.findAllCategories());
//        model.addAttribute("request", request);
//
//        // 2. 로그인한 사용자 정보 가져오기 (자동완성용)
//        if (principal != null) {
//            String memberId = principal.getName();
//            try {
//                Member member = memberService.findMember(memberId);
//                model.addAttribute("member", member); // ⭐️ 회원 정보를 모델에 담아 보냄
//            } catch (Exception e) {
//                // 회원을 못 찾으면 그냥 빈칸으로 두기 위해 아무것도 안 함
//            }
//        }
//
//        return "request/RequestForm";
//    }
//
//    // =======================================
//    // 2. [고객] 신청서 제출 (DB 저장)
//    // =======================================
//    @PostMapping("/requests/new")
//    public String createRequest(Request request) {
//        // 초기 상태는 무조건 'WAITING'
//        request.setStatus("WAITING");
//        requestRepository.save(request);
//        return "redirect:/"; // 제출이 끝나면 메인 페이지로 강제 이동(Redirect)
//    }

    // =======================================
    // 3. [관리자] 들어온 요청 목록 확인하기
    // =======================================
    @GetMapping("/requests")
    public String requestList(Model model, HttpServletRequest request) {
        // 2. request 객체를 "request"라는 이름으로 Model에 추가
        model.addAttribute("request", request);
        // 대기 중(WAITING)인 목록만 가져오기
        model.addAttribute("requests", requestRepository.findByStatusOrderByReqDateDesc("WAITING"));
        return "request/RequestList";
    }

    // =======================================
    // 4. 신청 반려 처리 (REJECT)
    // =======================================
    @GetMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id) {
        // 1. 신청서 찾기
        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 신청서입니다."));

        // 2. 상태 변경 (WAITING -> REJECTED)
        req.setStatus("REJECTED");
        requestRepository.save(req);

        return "redirect:/requests"; // 목록으로 복귀
    }
}

