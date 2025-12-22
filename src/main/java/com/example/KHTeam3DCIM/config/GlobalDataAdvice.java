package com.example.KHTeam3DCIM.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalDataAdvice {

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        // 모든 페이지에서 ${requestURI} 변수로 현재 주소를 쓸 수 있게 해줍니다.
        return request.getRequestURI();
    }
}