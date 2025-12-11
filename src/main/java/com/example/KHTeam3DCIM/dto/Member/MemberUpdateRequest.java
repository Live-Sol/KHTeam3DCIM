package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberUpdateRequest {
    private String password;    // 회원가입에 작성할 pw
    private String name;        // 회원가입에 작성할 이름
}
