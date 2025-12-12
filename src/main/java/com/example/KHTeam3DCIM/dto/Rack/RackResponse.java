package com.example.KHTeam3DCIM.dto.Rack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 조회용 DTO(전체 조회, 단일 조회 모두 사용 가능)
public class RackResponse {
    private Long id;            // 랙 ID
    private String rackName;    // 랙 이름
    private Long totalUnit;     // 랙의 총 유닛 수
    private String locationDesc;// 랙 위치 설명
    private Integer usedUnit;   // 랙에 사용 중인 유닛 수 (계산된 값)
}
