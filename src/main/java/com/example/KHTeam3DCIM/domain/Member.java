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
    private String name;        // 이름
    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;       // 이메일
    @Column(name = "CONTACT", nullable = false, length = 50)
    private String contact;     // 연락처

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // ⭐️ 관리자용 정보 통합 업데이트 메서드 ⭐️
    // 기존 updateName, updateRole 메서드를 대체하며, 추가된 필드까지 처리합니다.
    public void updateAdminInfo(String name, String email, String contact, Role role) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.role = role;
    }
}
