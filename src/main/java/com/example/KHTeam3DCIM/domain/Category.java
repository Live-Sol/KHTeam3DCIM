// Category.java (장비 종류 기준표)
// 파일 설명: "메뉴판 분류"입니다. 사용자가 장비를 등록할 때 '서버', '스위치', '스토리지' 중에서 고르게 하려고 만듭니다.

package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "DC_CATEGORY") // DB에 DC_CATEGORY 테이블 생성
@Getter // 데이터를 꺼내 볼 수 있게 함 (Setter가 없는 이유는? 기준정보는 함부로 바꾸면 안 되니까!)
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    // [1] 여기는 @GeneratedValue(자동생성)가 없습니다.
    // 왜냐하면 "SVR", "NET" 처럼 우리가 정한 약어를 직접 ID로 쓸 것이기 때문입니다.
    @Id
    @Column(name = "CATE_ID", length = 20)
    private String id; // 예: "SVR"

    @Column(name = "CATE_NAME", length = 50, nullable = false)
    private String name; // 예: "서버(Server)"

}