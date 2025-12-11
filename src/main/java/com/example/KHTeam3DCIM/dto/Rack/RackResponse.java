package com.example.KHTeam3DCIM.dto.Rack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 조회용 DTO(전체 조회, 단일 조회 모두 사용 가능)
public class RackResponse {
    private Long id;
    private String rackName;
    private Long totalUnit;
    private String locationDesc;
}
