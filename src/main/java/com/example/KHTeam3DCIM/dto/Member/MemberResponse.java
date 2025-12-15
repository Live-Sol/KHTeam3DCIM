package com.example.KHTeam3DCIM.dto.Member;

import com.example.KHTeam3DCIM.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 회원 정보 조회용(이용자 화면 출력) dto
public class MemberResponse {
    // 이름 (마스킹 처리된 값)
    private final String name;
    // 회사명 (마스킹 처리된 값)
    private final String companyName;
}
