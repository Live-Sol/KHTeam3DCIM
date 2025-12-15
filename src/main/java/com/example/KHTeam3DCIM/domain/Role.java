package com.example.KHTeam3DCIM.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // ⭐️ Description 필드를 추가하여 템플릿에서 사용하기 편리하게 합니다. ⭐️
    USER("일반 회원"),
    ADMIN("관리자");

    private final String description;

    // [참고] th:text="${role.description}" 대신 th:text="${role.getDescription()}"을 사용해야 할 수 있습니다.
    // 이는 Lombok (@Getter) 또는 명시적인 getter 메서드 구현 여부에 따라 달라집니다.
}