package com.example.KHTeam3DCIM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")         // 모든 주소는 /info 로 시작 합니다.
public class infoController {

    // 1. 이용 약관
    @GetMapping("/terms")
    public String terma(Model model) {
        model.addAttribute("pageTitle", "이용 약관");
        return "info/terms";
    }
    // 2. 법적 고지
    @GetMapping("/legal")
    public String legal(Model model) {
        model.addAttribute("pageTitle", "법적 고지");
        return "info/legal";
    }

    // 3. 개인정보처리방침
    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "개인정보 처리방침");
        return "info/privacy";
    }

    // 4. 고객센터
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "고객센터");
        return "info/contact";
    }


}
