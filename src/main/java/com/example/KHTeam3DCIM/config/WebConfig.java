package com.example.KHTeam3DCIM.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. [기존 유지] HTML Form에서 DELETE, PUT 요청을 사용하기 위한 필터
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    // 2. [추가 필수] 업로드된 이미지를 웹 브라우저에서 볼 수 있게 경로 연결
    // (이게 없으면 이미지가 404 에러로 안 보입니다)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹 주소 "/uploads/**" 로 요청이 오면 -> 실제 내 컴퓨터 "uploads/" 폴더를 보여줘라
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}