package com.example.KHTeam3DCIM.dto.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateRequest {
    private String password;    // 회원가입에 작성할 pw
    private String name;        // 회원가입에 작성할 이름
}
