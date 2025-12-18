package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.dto.Request.RequestDTO;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;

    // 1. 신청서 저장
    public void saveRequest(RequestDTO dto) {
        Request request = dto.toEntity();
        requestRepository.save(request);
    }

    // 2. 대기 중인 신청 목록 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<Request> findWaitingRequests() {
        return requestRepository.findByStatusOrderByReqDateDesc("WAITING");
    }

    // 3. [추가] 내 신청 이력 조회 (사용자용)
    @Transactional(readOnly = true)
    public List<Request> findMyRequests(String memberId) {
        return requestRepository.findByMemberIdOrderByReqDateDesc(memberId);
    }

    // 4. 단일 신청서 상세 조회
    @Transactional(readOnly = true)
    public Request findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청서가 존재하지 않습니다. ID: " + id));
    }

    // 5. 상태 업데이트
    public void updateStatus(Long id, String status) {
        Request request = findById(id);
        request.setStatus(status);
    }

    // 6. 요청 삭제
    public void deleteRequest(Long reqId) {
        if (!requestRepository.existsById(reqId)) {
            throw new IllegalArgumentException("삭제할 신청서가 없습니다.");
        }
        requestRepository.deleteById(reqId);
    }
}