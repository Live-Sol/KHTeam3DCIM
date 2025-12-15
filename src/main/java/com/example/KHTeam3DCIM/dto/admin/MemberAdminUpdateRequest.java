package com.example.KHTeam3DCIM.dto.admin;

import com.example.KHTeam3DCIM.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Lombok @Data를 사용해도 되지만, DTO에서는 불변성과 명확성을 위해
// @Getter, @Setter, @Builder를 명시적으로 사용하는 것을 권장합니다.

@Getter
@Setter
@Builder
//
public class MemberAdminUpdateRequest {

    // 기업명 (이름)
    @NotBlank(message = "기업명은 필수 입력 값입니다.")
    private String name;

    // 이메일
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // 연락처 (예: 전화번호 형식 검증)
    @NotBlank(message = "연락처는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처는 00-000-0000 형식으로 입력해 주세요.")
    private String contact;

    // 역할 (관리자 전용 수정 항목)
    @NotNull(message = "역할은 필수 선택 값입니다.")
    private Role role;
}