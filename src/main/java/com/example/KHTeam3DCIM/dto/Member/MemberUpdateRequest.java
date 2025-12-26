package com.example.KHTeam3DCIM.dto.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder // Lombok의 Builder 패턴 사용
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateRequest {
    // 1. 새 비밀번호 (선택적 수정)
    // DTO의 필드가 String 타입일 때, @Pattern은 null을 허용합니다.
    // 빈 문자열("")이 들어올 경우 @Pattern에 걸리게 되므로,
    // 서비스에서 Trim 후 처리하는 로직을 활용하고 @Pattern만 유지합니다.
    @Pattern(regexp = "^$|^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{5,20}$",
            message = "비밀번호는 영문자와 숫자를 모두 포함하여 5~20자여야 합니다.")
    private String newPassword;

    // 2. 담당자 이메일 (필수 수정)
    @Pattern(regexp = "^[^\s@]+@[^\s@]+\\.[^\s@]+$", message = "올바른 이메일 형식이 아닙니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 3. 담당자 직통 번호 (필수 수정)
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "담당자 번호 형식은 000-0000-0000 입니다.")
    private String contact;

    // 4. 회사명 (필수 수정)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]{2,30}$", message = "회사명은 2~30자의 한글, 영문, 숫자만 가능합니다.")
    private String companyName;

    // 5. 회사 대표 번호 (필수 수정)
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "회사 대표 번호 형식이 올바르지 않습니다.")
    private String companyPhone;

    // 6. 프로필 이미지 파일
    private MultipartFile profileImage;
    // [추가] 프로필 이미지 삭제 여부 확인 필드
    private Boolean deleteProfileImage;
}