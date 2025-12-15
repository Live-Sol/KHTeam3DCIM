package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DC_MEMBER")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @Column(name = "MEMBER_ID", nullable = false, length = 50)
    private String memberId;   // 아이디
    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;    // 암호
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;        // 담당자 성함
    @Column(name = "EMAIL",  nullable = false, length = 50)
    private String email;   // 이메일
    @Column(name = "CONTACT", length = 20)
    private String contact;     // 담당자 직통 번호
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // ⭐ [추가] 회사 정보 및 연락처
    @Column(name = "COMPANY_NAME", length = 50)
    private String companyName; // 회사명
    @Column(name = "COMPANY_PHONE", length = 20)
    private String companyPhone; // 회사 대표 번호


    // ⭐️ 관리자용 이름 업데이트 메서드 ⭐️
    public void updateName(String name) {
        this.name = name;
    }
    // ⭐️ 관리자용 역할 업데이트 메서드 추가 ⭐️
    public void updateRole(Role role) {
        this.role = role;
    }

}
