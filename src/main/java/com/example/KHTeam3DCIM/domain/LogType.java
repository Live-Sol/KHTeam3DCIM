package com.example.KHTeam3DCIM.domain;

public enum LogType {
    REQUEST_APPLY,      // 입주 신청 접수
    REQUEST_APPROVE,    // 입주 신청 승인/거절
    DEVICE_OPERATION,   // 장비/랙 상태 변경 (전원 ON/OFF 등)
    MEMBER_MANAGEMENT, MEMBERSHIP,  // 회원 정보 변경/권한 부여
    // ... 기타 필요한 유형 추가
}