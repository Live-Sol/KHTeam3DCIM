package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RequestService {
    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // 🌟 요청 ID를 받아 DB에서 영구적으로 삭제하는 트랜잭션 메서드
    @Transactional
    public void deleteRequest(Long reqId) {
        requestRepository.deleteById(reqId);
    }
}
