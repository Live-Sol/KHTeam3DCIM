// DeviceRepository.java (가장 중요)
// 장비를 저장하고, 조회하는 핵심 심부름꾼입니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Device;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // ==========================================
    // 1. 특정 랙(Rack)에 꽂힌 장비들만 다 가져와! (화면에 그림 그릴 때 사용)
    // ==========================================
    List<Device> findByRackId(Long rackId);

    // ==========================================
    // 2. 시리얼 번호로 장비 찾기 (검색용)
    // ==========================================
    Optional<Device> findBySerialNum(String serialNum);

    // ==========================================
    // 3. 통합 검색 (제조사 OR 모델명 OR 시리얼번호 포함 검색)
    // ==========================================
    /*Containing: 앞뒤로 % 붙여서 검색 (LIKE %keyword%)
    * IgnoreCase: 대소문자 구분 안 함*/
    List<Device> findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(String vendor, String modelName, String serialNum, Sort sort);

    // ==========================================
    // 4. 랙별 사용량(높이 합계) 계산 쿼리
    // ==========================================
    // COALESCE(..., 0)은 NULL일 경우 0으로 바꿔주는 안전장치입니다.
    @Query("SELECT COALESCE(SUM(d.heightUnit), 0) FROM Device d WHERE d.rack.id = :rackId")
    Integer getUsedUnitByRackId(@Param("rackId") Long rackId);

    // ==========================================
    // 5. [대시보드용 통계 쿼리 모음]
    // ==========================================

    // 5-1. 총 소비 전력 합계
    @Query("SELECT COALESCE(SUM(d.powerWatt), 0) FROM Device d")
    Integer sumTotalPower();

    // 5-2. 총 사용 중인 유닛(높이) 합계 (공간 효율 계산용)
    @Query("SELECT COALESCE(SUM(d.heightUnit), 0) FROM Device d")
    Integer sumTotalUsedHeight();

    // 5-3. 종류별 장비 개수 세기 (SVR, NET, STO, UPS)
    long countByCategory_Id(String cateId);

    // 5-4. 상태별 장비 개수 세기 (RUNNING, OFF)
    long countByStatus(String status);

    // 5-5. EMS 연동 장비 개수 세기 (EMS_STATUS가 'ON'인 것)
    long countByEmsStatus(String emsStatus);

    // ==========================================
    // [추가] 중복 검사용 메서드
    // ==========================================

    // 1. 시리얼 번호가 존재하는지 확인 (신규 등록 시 사용)
    boolean existsBySerialNum(String serialNum);

    // 2. 본인 ID를 제외하고 해당 시리얼 번호가 존재하는지 확인 (수정 시 사용)
    boolean existsBySerialNumAndIdNot(String serialNum, Long id);
}
