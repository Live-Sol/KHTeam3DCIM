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

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final RackService rackService;
    private final DeviceService deviceService;
    private final AuditLogService auditLogService;
    private final RequestRepository requestRepository;

    @GetMapping("/")
    public String home(Model model) {

        // 1. [기본 통계] 랙 개수, 대기 요청 건수
        long totalRacks = rackService.countAllRacks();
        long waitingRequests = requestRepository.countByStatus("WAITING");
        model.addAttribute("totalRacks", totalRacks);
        model.addAttribute("waitingRequests", waitingRequests);

        // 2. [대시보드 종합 통계] Service에서 Map으로 한 번에 받아옴
        // (장비개수, 비율, 에너지, PUE, EMS 등 모든 정보가 들어있음)
        Map<String, Object> stats = deviceService.getDashboardStatistics();
        model.addAttribute("stats", stats);

        // 3. 최근 로그
        model.addAttribute("recentLogs", auditLogService.getRecentActivityLogs(5));

        // 4. 로그인 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        boolean isLoggedIn = !loginId.equals("anonymousUser");

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("loginId", isLoggedIn ? loginId : "");

        return "index";
    }
}