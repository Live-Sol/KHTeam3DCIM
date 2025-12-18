package com.example.KHTeam3DCIM.dto.Rack;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 생성용 DTO
public class RackCreateRequest {
    @NotBlank(message = "랙 이름은 필수입니다.")
    @Size(max = 50, message = "랙 이름은 50자 이내여야 합니다.")
    private String rackName;

    @NotNull(message = "총 높이를 입력해주세요.")
    @Min(value = 1, message = "최소 1U 이상이어야 합니다.")
    @Max(value = 52, message = "표준 랙 최대 높이는 52U를 초과할 수 없습니다.")
    private Long totalUnit;

    private String locationDesc;
}
