package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 회원 가입 요청 dto
    public class MemberCreateRequest {
    private String memberId;    // 회원가입에 작성할 id
    private String password;    // 회원가입에 작성할 pw
    private String name;        // 회원가입에 작성할 이름
}
