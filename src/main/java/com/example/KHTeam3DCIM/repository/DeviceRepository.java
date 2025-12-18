// DeviceRepository.java (가장 중요)
// 장비를 저장하고, 조회하는 핵심 심부름꾼입니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Device;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // 1. 특정 랙의 장비 목록
    List<Device> findByRackId(Long rackId);

    // 2. 시리얼 번호 조회
    Optional<Device> findBySerialNum(String serialNum);

    // 3. 통합 검색 (제조사, 모델, 시리얼)
    List<Device> findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(
            String vendor, String modelName, String serialNum, Sort sort);

    // 4. 랙별 사용 유닛 합계
    @Query("SELECT COALESCE(SUM(d.heightUnit), 0) FROM Device d WHERE d.rack.id = :rackId")
    Integer getUsedUnitByRackId(@Param("rackId") Long rackId);

    // 5. 특정 랙에서 장비가 점유 중인 가장 높은 위치 조회 (높이 수정 제한용)
    @Query("SELECT COALESCE(MAX(d.startUnit + d.heightUnit - 1), 0) FROM Device d WHERE d.rack.id = :rackId")
    Integer getMaxUnitByRackId(@Param("rackId") Long rackId);

    // 6. 대시보드 통계
    @Query("SELECT COALESCE(SUM(d.powerWatt), 0) FROM Device d")
    Integer sumTotalPower();

    @Query("SELECT COALESCE(SUM(d.heightUnit), 0) FROM Device d")
    Integer sumTotalUsedHeight();

    long countByCategory_Id(String cateId);
    long countByStatus(String status);
    long countByEmsStatus(String emsStatus);

    // 7. 중복 검사
    boolean existsBySerialNum(String serialNum);
    boolean existsBySerialNumAndIdNot(String serialNum, Long id);
}
