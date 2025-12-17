package com.example.KHTeam3DCIM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/solutions") // 모든 주소 앞에 /solutions 가 붙습니다.
public class SolutionController {

    // 1. Rack Management 페이지
    @GetMapping("/racks")
    public String rackManagement(Model model) {
        model.addAttribute("pageTitle", "Smart Rack Management");
        return "solutions/rack_management"; // templates/solutions 폴더 안에 생성
    }

    // 2. Asset Tracking 페이지
    @GetMapping("/assets")
    public String assetTracking(Model model) {
        model.addAttribute("pageTitle", "Real-time Asset Tracking");
        return "solutions/asset_tracking";
    }

    // 3. Power Monitoring 페이지
    @GetMapping("/power")
    public String powerMonitoring(Model model) {
        model.addAttribute("pageTitle", "Intelligent Power Monitoring");
        return "solutions/power_monitoring";
    }

    // 4. Cooling System 페이지
    @GetMapping("/cooling")
    public String coolingSystem(Model model) {
        model.addAttribute("pageTitle", "Eco-Friendly Cooling System");
        return "solutions/cooling_system";
    }
}