package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.AuditLog;
import com.example.KHTeam3DCIM.service.AuditLogService;
import com.example.KHTeam3DCIM.service.MemberService;
import lombok.RequiredArgsConstructor; // ⭐️ Lombok의 RequiredArgsConstructor 사용 ⭐️
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor // ⭐️ final 필드 주입을 위한 어노테이션 추가 ⭐️
public class AdminController {

    // final 필드로 선언하고 @RequiredArgsConstructor를 사용하면 생성자가 대체됨
    private final AuditLogService auditLogService;
    private final MemberService memberService; // (사용되지 않지만 기존처럼 유지)

    @GetMapping
    public String adminDashboard(Model model) { // ⭐️ HttpSession 제거 ⭐️

        // 🚨 권한 체크 로직 제거: Spring SecurityConfig가 이미 hasRole('ADMIN')을 검사했음.

        // 🚨 헤더용 모델 속성 제거: header.html이 sec:authorize로 정보를 직접 가져감.

        // --- 4. 통계/로그 데이터 추가 (데이터 처리만 남김) ---
        int pendingRequestCount = auditLogService.getPendingRequestCount();
        int totalDeviceCount = auditLogService.getTotalDeviceCount();
        int totalMemberCount = auditLogService.getTotalMemberCount();
        int logLimit = 5;
        List<AuditLog> recentLogs = auditLogService.getRecentActivityLogs(logLimit);

        model.addAttribute("pageTitle", "대시보드 홈");
        model.addAttribute("pendingRequestCount", pendingRequestCount);
        model.addAttribute("totalDeviceCount", totalDeviceCount);
        model.addAttribute("totalMemberCount", totalMemberCount);
        model.addAttribute("recentLogs", recentLogs);

        return "admin"; // templates/admin.html
    }
}