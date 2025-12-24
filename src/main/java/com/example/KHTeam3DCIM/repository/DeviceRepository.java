// DeviceRepository.java (가장 중요)
// 장비를 저장하고, 조회하는 핵심 심부름꾼입니다.

package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.domain.Rack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // [추가] 전체 조회 시 연관 객체(Member, Category, Rack)를 한 번에 가져오기
    @Query("SELECT d FROM Device d " +
            "JOIN FETCH d.member " +
            "JOIN FETCH d.category " +
            "LEFT JOIN FETCH d.rack")
    List<Device> findAllWithMember(Sort sort);

    // [추가] 검색 시 연관 객체를 포함하고, 신청자 아이디(memberId)로도 검색 가능하게 설정
    @Query("SELECT d FROM Device d " +
            "JOIN FETCH d.member " +
            "JOIN FETCH d.category " +
            "LEFT JOIN FETCH d.rack " +
            "WHERE LOWER(d.vendor) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(d.modelName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(d.serialNum) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(d.member.memberId) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Device> findAllWithMemberByKeyword(@Param("kw") String kw, Sort sort);

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

    // 8. 아이디 조회
    @Query("SELECT d FROM Device d JOIN FETCH d.member JOIN FETCH d.category LEFT JOIN FETCH d.rack")
    List<Device> findAllWithMember();

    // 9. 랙 위치 중복 체크
    @Query("""
        SELECT COUNT(d) > 0
        FROM Device d
        WHERE d.rack = :rack
          AND d.startUnit <= :endUnit
          AND (d.startUnit + d.heightUnit - 1) >= :startUnit
    """)
    boolean existsOverlappingDevice(
            @Param("rack") Rack rack,
            @Param("startUnit") int startUnit,
            @Param("endUnit") int endUnit
    );

    // 10. 특정 사용자의 장비 목록 조회 (페이징 지원)
    // status가 'DELETED'인 것도 내역 확인을 위해 포함하거나, 필요에 따라 구분할 수 있습니다.
    @Query("SELECT d FROM Device d " +
            "JOIN FETCH d.category " +
            "LEFT JOIN FETCH d.rack " +
            "WHERE d.member.memberId = :memberId " +
            "AND (LOWER(d.vendor) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(d.modelName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(d.serialNum) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<Device> findMyDevicesByKeyword(@Param("memberId") String memberId, @Param("kw") String kw, Pageable pageable);

    @Query("SELECT d FROM Device d " +
            "JOIN FETCH d.category " +
            "LEFT JOIN FETCH d.rack " +
            "WHERE d.member.memberId = :memberId")
    Page<Device> findMyDevices(@Param("memberId") String memberId, Pageable pageable);

    // 이미 삭제된 장비를 제외하고, 계약 정보가 있는 장비만 조회
    @Query("SELECT d FROM Device d WHERE d.status != 'DELETED' AND d.contractDate IS NOT NULL")
    List<Device> findAllActiveDevices();
}
