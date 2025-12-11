package com.example.KHTeam3DCIM.dto.Rack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RackUpdateRequest {
    private String rackName;
    private Long totalUnit;
    private String locationDesc;
}
