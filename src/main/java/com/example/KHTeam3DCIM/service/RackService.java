package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RackService {

    private final RackRepository rackRepository;

    // 1. 전체 랙 조회
    public List<Rack> findAllRacks() {
        return rackRepository.findAll();
    }

    // 2. 랙 아이디로 조회 (존재하지 않으면 예외 발생)
    public Rack findByIdOrThrow(Long id) {
        return rackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack이 존재하지 않습니다."));
    }

    // 3. 랙 추가
    public Rack addRack(Rack rack) {
        return rackRepository.save(rack);
    }

    // 4. 랙 정보 수정 (전체 업데이트)
    // put은 일단 작성했지만, patch가 있기에 생략 필요하다면 활성화해서 이용 가능한 부분
//    public Rack updateRack(Rack rack) {
//        if (!rackRepository.existsById(rack.getid())) {
//            throw new RuntimeException("Rack이 존재하지 않습니다.");
//        }
//        return rackRepository.save(rack);
//    }

    // 4-1. 랙 부분 수정 (Patch 용)
    @Transactional
    public Rack updateRackPartially(Long id, Rack patch) {
        return rackRepository.findById(id)
                .map(existing -> {
                    if (patch.getRackName() != null) existing.setRackName(patch.getRackName());
                    if (patch.getTotalUnit() != null) existing.setTotalUnit(patch.getTotalUnit());
                    if (patch.getLocationDesc() != null) existing.setLocationDesc(patch.getLocationDesc());
                    return rackRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Rack이 존재하지 않습니다."));
    }

    // 5. 랙 삭제
    public void deleteRack(Long id) {
        if (!rackRepository.existsById(id)) {
            throw new RuntimeException("Rack이 존재하지 않습니다.");
        }
        rackRepository.deleteById(id);
    }
}
