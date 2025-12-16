package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 회원 가입 요청 dto
public class MemberCreateRequest {
    private String memberId;    // 아이디
    private String password;    // 비밀번호
    private String name;        // 이름
    private String email;       // 이메일
    private String companyName; // 회사명
    private String companyPhone; // 회사 대표 번호
    private String contact;     // 담당자 연락처
}