package com.example.KHTeam3DCIM.dto.admin;

import com.example.KHTeam3DCIM.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 관리자용 이용자 정보 조회
public class MemberAdminResponse {
    private final String memberId;
    private final String name;
    private final String email;
    private final String contact;
    private final String companyName;
    private final String companyPhone;
    private final Role role;
}
