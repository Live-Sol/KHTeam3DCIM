// 메인 안내 데스크: MainController.java
// http://localhost:8080/ (맨 처음 접속) 했을 때 에러 페이지가 안 뜨게 잡아주는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
