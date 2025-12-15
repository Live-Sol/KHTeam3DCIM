package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.service.AuditLogService;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.RackService;
// RequestService가 없다면 Repository를 써야겠지만, 가능하면 Service를 만드는 게 좋습니다.
// 여기서는 빠른 수정을 위해 Repository 사용을 최소화하는 방향으로 갑니다.
import com.example.KHTeam3DCIM.repository.RequestRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    // Service 계층 주입 (Repository 직접 접근 지양)
    private final RackService rackService;
    private final DeviceService deviceService;
    private final RequestRepository requestRepository; // RequestService가 없어서 부득이하게 사용
    private final AuditLogService auditLogService;

    @GetMapping("/")
    public String home(Model model) {
        // 1. 통계 데이터 (Service에 count 메서드가 없으면 추가하는 게 정석입니다)
        // 현재는 Repository가 Service 안에 숨겨져 있으므로,
        // 간단히 처리하기 위해 Service에 count 메서드를 추가하거나, 여기서만 예외적으로 Repository를 쓸 수도 있습니다.
        // 하지만 "대수술"이므로 정석대로라면 Service에 위임해야 합니다.
        // (DeviceService, RackService에 count 기능이 없으므로 일단 size()로 대체하거나 repo를 써야 함)

        long totalRacks = rackService.findAllRacks().size(); // 성능상 count쿼리가 좋지만 일단 이렇게
        long totalDevices = deviceService.findAllDevices("latest", "desc").size();
        long waitingRequests = requestRepository.countByStatus("WAITING");

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