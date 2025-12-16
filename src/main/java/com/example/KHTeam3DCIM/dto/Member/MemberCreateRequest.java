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
    private String email;       // 회원가입에 작성할 이메일
    private String companyName; // 회사명
    private String companyPhone; // 회사 대표 번호
    private String contact;     // 담당자 연락처
}