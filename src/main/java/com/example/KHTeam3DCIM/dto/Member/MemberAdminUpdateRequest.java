package com.example.KHTeam3DCIM.dto.Member;

import com.example.KHTeam3DCIM.domain.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberAdminUpdateRequest {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]{2,10}$", message = "이름은 한글과 알파벳만 가능하며, 2~10글자여야 합니다.")
    private String name;

    @NotNull(message = "역할(Role)은 필수 선택 항목입니다.")
    private Role role;

    // 이 DTO는 폼 바인딩(`@ModelAttribute`)을 위해 사용되므로,
    // ID는 @PathVariable로 따로 받기 때문에 여기에 포함하지 않습니다.
}