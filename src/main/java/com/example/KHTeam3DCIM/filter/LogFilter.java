package com.example.KHTeam3DCIM.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component  // 로그가 자동으로 작성되므로 @ServletComponentScan 을 생략해도 됨
@Slf4j
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HttpServletRequest와 HttpServletResponse로 변환
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 요청 처리 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // 요청 정보 로그 출력
        log.info("[{}] REQUEST: {} {}", LocalDateTime.now(), req.getMethod(), req.getRequestURI());

        // 다음 필터 또는 컨트롤러 실행
        chain.doFilter(request, response);

        // 처리 시간 계산
        long duration = System.currentTimeMillis() - startTime;

        // 응답 상태 코드와 처리 시간 로그 출력
        log.info("[{}] RESPONSE: {} 처리시간: {}ms", LocalDateTime.now(), res.getStatus(), duration);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 시 필요한 로직이 있으면 추가 가능
        log.info("LoggingFilter initialized");
    }

    @Override
    public void destroy() {
        // 필터 종료 시 필요한 로직이 있으면 추가 가능
        log.info("LoggingFilter destroyed");
    }
}
