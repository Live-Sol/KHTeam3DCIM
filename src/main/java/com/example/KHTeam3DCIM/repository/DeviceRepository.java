// DeviceRepository.java (가장 중요)
// 장비를 저장하고, 조회하는 핵심 심부름꾼입니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // 1. 특정 랙(Rack)에 꽂힌 장비들만 다 가져와! (화면에 그림 그릴 때 사용)
    List<Device> findByRackId(Long rackId);

    // 2. 시리얼 번호로 장비 찾기 (검색용)
    Optional<Device> findBySerialNum(String serialNum);

    // 3. 통합 검색 (제조사 OR 모델명 OR 시리얼번호 포함 검색)
    // Containing: 앞뒤로 % 붙여서 검색 (LIKE %keyword%)
    // IgnoreCase: 대소문자 구분 안 함
    List<Device> findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(String vendor, String modelName, String serialNum);
}
