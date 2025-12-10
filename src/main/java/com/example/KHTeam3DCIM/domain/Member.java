package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DC_MEMBE")
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

    public enum Role {
        ADMIN,
        USER
    }

}
