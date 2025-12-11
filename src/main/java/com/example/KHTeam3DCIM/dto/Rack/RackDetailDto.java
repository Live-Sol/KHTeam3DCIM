// 42칸 중 '한 칸'을 표현할 **바구니(DTO)**

package com.example.KHTeam3DCIM.dto.Rack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RackDetailDto {
    private int unitNum;       // 층수 (42, 41 ... 1)
    private String status;     // 상태 (EMPTY:빈칸, FULL:장비있음, SKIP:합쳐진칸)
    private String deviceName; // 장비 이름 (없으면 "빈 슬롯")
    private String type;       // 장비 종류 (SVR, NET...) - 색깔 다르게 하려고
    private int rowSpan;       // 몇 칸 차지하는지 (HTML rowspan용)
    private Long deviceId;     // 클릭하면 상세페이지로 이동하려고
}