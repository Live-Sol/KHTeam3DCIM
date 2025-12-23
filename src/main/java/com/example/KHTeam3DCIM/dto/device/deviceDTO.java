package com.example.KHTeam3DCIM.dto.device;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class deviceDTO {
    // 체크박스로 선택된 장비들의 ID 리스트
    private List<Long> ids;

    // 변경하고자 하는 EMS 상태 (ON / OFF)
    private String emsStatus;

    // 변경하고자 하는 장비 상태 (RUNNING / STOPPED 등)
    private String status;
}