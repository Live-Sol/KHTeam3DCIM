package com.example.KHTeam3DCIM.dto.Rack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 생성용 DTO
public class RackCreateRequest {
    private String rackName;
    private Long totalUnit;
    private String locationDesc;
}
