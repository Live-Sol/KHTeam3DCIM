package com.example.KHTeam3DCIM.dto.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 관리자 페이지에서 특정 회원 ID로 검색(Find By Id)하기 위한 요청 DTO
 */
@Getter
@Setter
@ToString
public class MemberFindByIdAdmin {

    /**
     * 검색할 회원 ID (memberId)
     */
    private String searchId;

    // 기본 생성자
    public MemberFindByIdAdmin() {}

    // 모든 필드 생성자 (옵션)
    public MemberFindByIdAdmin(String searchId) {
        this.searchId = searchId;
    }
}