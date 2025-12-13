package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final RackRepository rackRepository;
    private final DeviceRepository deviceRepository;
    private final RequestRepository requestRepository;
    private final AuditLogService auditLogService; // ⭐️ AuditLog 사용

    @GetMapping("/")
    public String home(Model model) {

        // 1. 통계 데이터
        long totalRacks = rackRepository.count();
        long totalDevices = deviceRepository.count();
        long waitingRequests = requestRepository.countByStatus("WAITING");

        // 2. 최근 로그 (AuditLogService 이용)
        model.addAttribute("recentLogs", auditLogService.getRecentActivityLogs(5));

        // 3. 모델 담기
        model.addAttribute("totalRacks", totalRacks);
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("waitingRequests", waitingRequests);

        // 4. 로그인 정보 (Spring Security 방식)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName(); // 로그인 안 했으면 "anonymousUser"

        // 로그인 여부 확인 (anonymousUser가 아니면 로그인 한 것)
        boolean isLoggedIn = !loginId.equals("anonymousUser");

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("loginId", isLoggedIn ? loginId : "");
        // 권한 정보 등은 필요하면 auth.getAuthorities()로 꺼낼 수 있음

        return "index";
    }
}