package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.AuditLogService;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    // 🚑 [수술 완료] Service를 통해 데이터를 가져오도록 구조 개선
    private final RackService rackService;
    private final DeviceService deviceService;
    private final AuditLogService auditLogService;
    private final RequestRepository requestRepository; // (RequestService 미구현으로 예외적 허용)

    @GetMapping("/")
    public String home(Model model) {

        // 1. 통계 데이터 (Service 호출)
        long totalRacks = rackService.countAllRacks();     // 랙 개수
        long totalDevices = deviceService.countAllDevices(); // 장비 개수
        long waitingRequests = requestRepository.countByStatus("WAITING"); // 대기 요청

        // 2. 최근 로그
        model.addAttribute("recentLogs", auditLogService.getRecentActivityLogs(5));

        // 3. 모델 담기
        model.addAttribute("totalRacks", totalRacks);
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("waitingRequests", waitingRequests);

        // 4. 로그인 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        boolean isLoggedIn = !loginId.equals("anonymousUser");

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("loginId", isLoggedIn ? loginId : "");

        return "index";
    }
}