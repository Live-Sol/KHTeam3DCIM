package com.example.KHTeam3DCIM.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
// 엑세스 키 필터
public class AccessKeyFilter implements Filter {

    // 액세스 키를 저장할 위치 (예시)
    private static final String VALID_ACCESS_KEY = "YOUR_SECRET_ACCESS_KEY";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 요청에서 액세스 키 추출 (헤더나 쿼리 파라미터에서)
        String accessKey = req.getHeader("X-ACCESS-KEY");
        if (accessKey == null) {
            // 액세스 키가 없으면 403 에러 반환
            log.error("Access key missing");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            return;
        }

        // 액세스 키 검증
        if (!VALID_ACCESS_KEY.equals(accessKey)) {
            // 액세스 키가 잘못되었으면 403 에러 반환
            log.error("Invalid access key");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            return;
        }

        // 액세스 키가 유효하면 다음 필터 또는 서블릿 실행
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 시 필요한 로직 (필요 시 추가)
    }

    @Override
    public void destroy() {
        // 필터 종료 시 필요한 로직 (필요 시 추가)
    }
}
