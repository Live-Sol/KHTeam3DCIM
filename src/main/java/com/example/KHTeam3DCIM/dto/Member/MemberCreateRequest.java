package com.example.KHTeam3DCIM.dto.Member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 회원 가입 요청 dto
    public class MemberCreateRequest {
        // 기존 필드
        @NotBlank(message = "아이디는 필수 입력 항목입니다.")
        @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 알파벳 소문자와 숫자만 가능하며, 4~20글자여야 합니다.")
        private String memberId;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        // 비밀번호 복잡도 규칙 (예시)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{4,}$", message = "비밀번호는 4자 이상, 영문자와 숫자를 포함해야 합니다.")
        private String password;

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣]{2,10}$", message = "이름은 한글과 알파벳만 가능하며, 2~10글자여야 합니다.")
        private String name;

        // ⭐️ 1. Email 필드 추가 및 유효성 검사 ⭐️
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        // ⭐️ 2. 연락처 필드 추가 및 유효성 검사 ⭐️
        @NotBlank(message = "연락처는 필수 입력 항목입니다.")
        @Pattern(regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$|^[0-9]{10,11}$",
                message = "연락처 형식이 올바르지 않습니다. (예: 010-xxxx-xxxx 또는 010xxxxxxxx)")
        private String contact;
}
