// 파일의 역할 : 신청서(Request) 작성 및 관리와 관련된 요청을 처리하는 컨트롤러 클래스

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RequestController {

    private final RequestRepository requestRepository;
    private final CategoryService categoryService;

    // =======================================
    // 1. [고객] 입주 신청서 작성 화면
    // =======================================
    @GetMapping("/requests/new")
    public String requestForm(Model model) {
        // 장비 종류(서버, 스위치 등) 선택해야 하니까 카테고리 정보 넘김
        model.addAttribute("categories", categoryService.findAllCategories());
        return "request/RequestForm";
    }

    // =======================================
    // 2. [고객] 신청서 제출 (DB 저장)
    // =======================================
    @PostMapping("/requests/new")
    public String createRequest(Request request) {
        // 초기 상태는 무조건 'WAITING'
        request.setStatus("WAITING");
        requestRepository.save(request);
        return "redirect:/"; // 제출이 끝나면 메인 페이지로 강제 이동(Redirect)
    }

    // =======================================
    // 3. [관리자] 들어온 요청 목록 확인하기
    // =======================================
    @GetMapping("/requests")
    public String requestList(Model model) {
        // 대기 중(WAITING)인 목록만 가져오기
        model.addAttribute("requests", requestRepository.findByStatusOrderByReqDateDesc("WAITING"));
        return "request/RequestList";
    }


}

