package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "DC_CATEGORY")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @Column(name = "CATE_ID", length = 20)
    private String id; // SVR, NET 등 직접 입력받는 코드라 Sequence 안 씀

    @Column(name = "CATE_NAME", length = 50, nullable = false)
    private String name; // Server, Network
}