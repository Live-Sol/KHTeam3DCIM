package com.example.KHTeam3DCIM.dto.Rack;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 부분 수정용 DTO
public class RackUpdateRequest {
    @NotBlank(message = "수정할 이름을 입력하세요.")
    private String rackName;

    @NotNull(message = "높이 값은 필수입니다.")
    @Min(1)
    private Long totalUnit;

    private String locationDesc;
}
