package com.example.KHTeam3DCIM.config;

import com.example.KHTeam3DCIM.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor // final 필드(RequestService) 생성자 주입을 위해 추가
public class GlobalDataAdvice {

    private final RequestService requestService; // 서비스 주입

    // 1. 기존 기능: 현재 URI 정보 제공
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // 2. [추가된 기능] 관리자 접속 시 대기 중인 신청 건수 제공
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자이고, 권한 중에 'ROLE_ADMIN'이 있는 경우
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            long count = requestService.countWaitingRequests();
            model.addAttribute("globalPendingCount", count);
        }
    }
}