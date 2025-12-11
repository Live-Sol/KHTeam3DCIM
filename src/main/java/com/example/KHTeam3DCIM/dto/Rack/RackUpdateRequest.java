package com.example.KHTeam3DCIM.dto.Rack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 부분 수정용 DTO
public class RackUpdateRequest {
    private String rackName;
    private Long totalUnit;
    private String locationDesc;
}
