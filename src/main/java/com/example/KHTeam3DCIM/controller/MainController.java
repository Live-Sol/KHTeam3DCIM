package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.DcimEnvironment;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.AuditLogService;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.EnvironmentService; // [추가]
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
    private final EnvironmentService envService; // [추가] 환경 서비스 주입

    @GetMapping("/")
    public String home(Model model) {

        // 1. [기본 통계]
        long totalRacks = rackService.countAllRacks();
        long waitingRequests = requestRepository.countByStatus("WAITING");
        model.addAttribute("totalRacks", totalRacks);
        model.addAttribute("waitingRequests", waitingRequests);

        // 2. [대시보드 종합 통계] (차트용 데이터)
        Map<String, Object> stats = deviceService.getDashboardStatistics();
        model.addAttribute("stats", stats);

        // 3. 최근 로그
        model.addAttribute("recentLogs", auditLogService.getRecentActivityLogs(5));

        // 4. 로그인 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();
        boolean isLoggedIn = !loginId.equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("loginId", loginId);

        // ⭐ 5. [추가] 환경 정보 (PUE, 온도) 가져오기
        DcimEnvironment env = envService.getEnvironment();
        // 시뮬레이션 돌려서 최신값 반영
        env = envService.calculateSimulation(env);
        model.addAttribute("env", env);

        return "index"; // templates/index.html
    }
}