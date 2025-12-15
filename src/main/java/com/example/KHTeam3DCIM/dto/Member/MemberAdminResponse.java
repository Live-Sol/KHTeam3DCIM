package com.example.KHTeam3DCIM.dto.Member;

import com.example.KHTeam3DCIM.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 관리자용 이용자 정보 조회
public class MemberAdminResponse {
    private String memberId;
    private String name;
    private String email;
    private String contact;
    private Role role;
}
