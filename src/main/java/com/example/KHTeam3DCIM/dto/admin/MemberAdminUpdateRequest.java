package com.example.KHTeam3DCIM.dto.admin;

import com.example.KHTeam3DCIM.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

// Lombok @Data를 사용해도 되지만, DTO에서는 불변성과 명확성을 위해
// @Getter, @Setter, @Builder를 명시적으로 사용하는 것을 권장합니다.

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAdminUpdateRequest {

    @NotBlank(message = "담당자 성함은 필수 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]{2,10}$", message = "이름은 한글/영문 2~10자여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처는 000-0000-0000 형식이어야 합니다.")
    private String contact; // 담당자 직통 번호

    @NotNull(message = "권한은 필수 항목입니다.")
    private Role role; // 관리자는 Role까지 변경 가능

    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]{2,30}$", message = "회사명은 2~30자여야 합니다.")
    private String companyName;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "회사 대표 번호 형식이 올바르지 않습니다.")
    private String companyPhone; // 회사 대표 번호
}
