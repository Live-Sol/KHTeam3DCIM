package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 로그인 요청 dto
public class MemberLoginRequest {
    private String memberId;    // 로그인시 필요한 id
    private String password;    // 로그인시 필요한 pw
}
