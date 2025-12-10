package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RackService {

    private final RackRepository rackRepository;

    // 1. 전체 랙 조회
    public List<Rack> findAllRacks() {
        return rackRepository.findAll();
    }

    // 2. 랙 아이디로 조회
    public Optional<Rack> findById(Long rackId) {
        return rackRepository.findById(rackId);
    }

    // 3. 랙 추가
    public Rack addRack(Rack rack) {
        return rackRepository.save(rack);
    }

    // 4. 랙 수정
    public Rack updateRack(Rack rack) {
        // 필요시 존재 여부 확인 후 수정
        return rackRepository.save(rack);
    }

    // 5. 랙 삭제
    public void deleteRack(Long rackId) {
        rackRepository.deleteById(rackId);
    }
}
