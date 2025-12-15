package com.example.KHTeam3DCIM.dto.Member;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder // Lombok의 Builder 패턴 사용
public class MemberUpdateRequest {
//
    // 비밀번호는 변경 시에만 사용되므로 필수는 아니지만, 입력되면 유효성 검사 적용
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하이어야 합니다.")
    private String password;

    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하이어야 합니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 알파벳만 가능합니다.")
    private String name;

    // ⭐️ 추가된 필드: 이메일 ⭐️
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    @Size(max = 50, message = "이메일은 50자 이하이어야 합니다.")
    private String email;

    // ⭐️ 추가된 필드: 연락처 ⭐️
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식(000-0000-0000)이 올바르지 않습니다.")
    private String contact;

    // 참고: 일반 회원 수정에서는 Role을 변경할 수 없으므로 Role 필드는 포함하지 않습니다.
}