// 메인 안내 데스크: MainController.java
// http://localhost:8080/ (맨 처음 접속) 했을 때 에러 페이지가 안 뜨게 잡아주는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.repository.DcLogRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final RackRepository rackRepository;
    private final DeviceRepository deviceRepository;
    private final RequestRepository requestRepository;
    private final DcLogRepository dcLogRepository;

    @GetMapping("/")
    public String home(Model model) {

        // 1. 상단 카드용 숫자 데이터 조회
        long totalRacks = rackRepository.count();                           // 총 랙 개수
        long totalDevices = deviceRepository.count();                       // 총 장비 개수
        long waitingRequests = requestRepository.countByStatus("WAITING");  // 대기중인 신청

        // 2. 하단 리스트용 데이터 조회
        model.addAttribute("recentLogs", dcLogRepository.findTop5ByOrderByLogDateDesc()); // 최근 활동 로그 5개

        // 3. 모델에 담기
        model.addAttribute("totalRacks", totalRacks);           // 총 랙 개수
        model.addAttribute("totalDevices", totalDevices);       // 총 장비 개수
        model.addAttribute("waitingRequests", waitingRequests); // 대기중인 신청 개수

        return "index";
    }
}
