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
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // ⭐️ 관리자용 이름 업데이트 메서드 ⭐️
    public void updateName(String name) {
        this.name = name;
    }
    // ⭐️ 관리자용 역할 업데이트 메서드 추가 ⭐️
    public void updateRole(Role role) {
        this.role = role;
    }
}
