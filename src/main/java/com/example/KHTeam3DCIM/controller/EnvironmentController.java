package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.DcimEnvironment;
import com.example.KHTeam3DCIM.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class EnvironmentController {

    private final EnvironmentService envService;

    // 1. [냉방 관리] 페이지 이동
    @GetMapping("/cooling")
    public String coolingPage(Model model) {
        // 현재 환경 상태를 가져와서 화면에 전달
        DcimEnvironment env = envService.getEnvironment();
        // 화면 열 때마다 시뮬레이션 최신화
        env = envService.calculateSimulation(env);

        model.addAttribute("env", env);
        model.addAttribute("pageTitle", "Cooling System Management");
        return "admin/cooling"; // templates/admin/cooling.html
    }

    // 2. [냉방 설정] 업데이트 처리 (POST)
    @PostMapping("/cooling/update")
    public String updateCooling(@RequestParam("targetTemp") Double targetTemp,
                                @RequestParam("fanSpeed") Integer fanSpeed,
                                @RequestParam("coolingMode") String coolingMode) {

        // 서비스에게 설정 변경 요청
        envService.updateCoolingSettings(targetTemp, fanSpeed, coolingMode);

        return "redirect:/admin/cooling"; // 처리 후 다시 냉방 페이지로
    }

    // 3. [PUE 모니터링] 페이지 이동
    @GetMapping("/pue")
    public String puePage(Model model) {
        DcimEnvironment env = envService.getEnvironment();
        env = envService.calculateSimulation(env);

        model.addAttribute("env", env);
        model.addAttribute("pageTitle", "Power Usage Effectiveness (PUE)");
        return "admin/pue"; // templates/admin/pue.html
    }

    // 4. [API] 실시간 데이터 요청 (JS에서 1초마다 호출용)
    @GetMapping("/api/env/now")
    @ResponseBody
    public DcimEnvironment getCurrentEnv() {
        // 현재 상태를 JSON으로 반환
        return envService.calculateSimulation(null);
    }
}