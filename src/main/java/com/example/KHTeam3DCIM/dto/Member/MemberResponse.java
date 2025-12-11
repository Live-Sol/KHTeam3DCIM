package com.example.KHTeam3DCIM.dto.Member;

import com.example.KHTeam3DCIM.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 회원 정보 조회용(이용자 화면 출력) dto
public class MemberResponse {
    private String name;    // XX 님 환영합니다 용도
    private Role role;    // XX 님 환영합니다 옆에 유저인지 관리자인지 출력용도? (생략 가능)
}
