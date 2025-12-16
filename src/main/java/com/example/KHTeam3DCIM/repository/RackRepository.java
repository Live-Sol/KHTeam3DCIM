package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Rack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RackRepository extends JpaRepository<Rack,Long> {

    // 모든 랙의 총 높이(Total Unit) 합계 구하기 (전체 공간 계산용)
    @Query("SELECT COALESCE(SUM(r.totalUnit), 0) FROM Rack r")
    Long sumTotalRackUnit();
}