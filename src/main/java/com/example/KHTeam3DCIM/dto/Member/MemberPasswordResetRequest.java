package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberPasswordResetRequest {
    private String memberId;    // 아이디
    private String name;        // 이름 (본인 확인용)
    private String email;       // 이메일 (본인 확인용)
    private String newPassword; // 새 비밀번호
}