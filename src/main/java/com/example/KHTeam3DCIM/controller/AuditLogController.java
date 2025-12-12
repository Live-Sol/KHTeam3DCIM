/*
package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.entity.AuditLog;
import com.example.KHTeam3DCIM.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

*/
/**
 * 관리자 페이지(Admin Dashboard)를 위한 컨트롤러
 * URL: /admin
 *//*

@Controller
@RequestMapping("/admin")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // 생성자 주입
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    */
/**
     * 관리자 대시보드 홈 페이지를 반환합니다.
     *//*

    @GetMapping
    public String adminDashboard(Model model) {

        // 1. 통계 데이터 로드 (Service 호출)
        int pendingRequestCount = auditLogService.getPendingRequestCount();
        int totalDeviceCount = auditLogService.getTotalDeviceCount();
        int totalMemberCount = auditLogService.getTotalMemberCount();

        // 2. 최근 활동 로그 로드 (Service 호출)
        int logLimit = 5; // 화면에 표시할 로그 개수
        List<AuditLog> recentLogs = auditLogService.getRecentActivityLogs(logLimit);
        // List<AuditLog> import가 필요할 수 있습니다. (AuditLog의 실제 패키지 경로로)

        // 모델에 데이터 추가
        model.addAttribute("pageTitle", "대시보드 홈");
        model.addAttribute("pendingRequestCount", pendingRequestCount);
        model.addAttribute("totalDeviceCount", totalDeviceCount);
        model.addAttribute("totalMemberCount", totalMemberCount);
        model.addAttribute("recentLogs", recentLogs); // 로그 목록 추가

        return "admin"; // 뷰 이름 반환
    }
}*/
