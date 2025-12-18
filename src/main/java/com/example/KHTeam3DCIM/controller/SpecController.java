package com.example.KHTeam3DCIM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/specs") // 모든 주소 앞에 /specs 가 붙습니다.
public class SpecController {

    // 1. 랙 장비 제원
    @GetMapping("/racks")
    public String rackSpecs(Model model) {
        model.addAttribute("pageTitle", "Rack Specifications");
        return "specs/racks"; // templates/specs/racks.html
    }

    // 2. 서버 장비 제원
    @GetMapping("/servers")
    public String serverSpecs(Model model) {
        model.addAttribute("pageTitle", "Server Specifications");
        return "specs/servers";
    }

    // 3. 배전 장치 제원
    @GetMapping("/power")
    public String powerSpecs(Model model) {
        model.addAttribute("pageTitle", "Power System Specs");
        return "specs/power";
    }

    // 4. 공조 장치 제원
    @GetMapping("/cooling")
    public String coolingSpecs(Model model) {
        model.addAttribute("pageTitle", "Cooling System Specs");
        return "specs/cooling";
    }
}