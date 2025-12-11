package com.example.KHTeam3DCIM.config;

import com.example.KHTeam3DCIM.filter.AccessKeyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Spring 설정 클래스
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AccessKeyFilter> accessKeyFilter() {
        // FilterRegistrationBean을 사용하여 필터 등록
        FilterRegistrationBean<AccessKeyFilter> registrationBean = new FilterRegistrationBean<>();

        // 필터 객체 생성
        registrationBean.setFilter(new AccessKeyFilter());

        // 필터가 적용될 URL 패턴 지정
        registrationBean.addUrlPatterns("/api/*");  // "/api/*" 패턴의 요청에만 적용

        // 필터 초기화 파라미터 설정
        registrationBean.addInitParameter("allowedAccessKey", "YOUR_SECRET_ACCESS_KEY"); // 액세스 키 값 전달

        // 필터 실행 순서 설정 (숫자가 낮을수록 먼저 실행)
        registrationBean.setOrder(1);  // 1이 가장 우선순위

        return registrationBean;  // 필터 등록 후 반환
    }
}
