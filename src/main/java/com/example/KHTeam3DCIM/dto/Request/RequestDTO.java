package com.example.KHTeam3DCIM.dto.Request;

import com.example.KHTeam3DCIM.domain.Request;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDTO {

    // 1. 신청자 정보
    @NotBlank(message = "회사명은 필수 입력 항목입니다.")
    private String companyName;

    private String companyPhone;

    @NotBlank(message = "담당자 성함은 필수 입력 항목입니다.")
    private String userName;

    @NotBlank(message = "담당자 연락처는 필수 입력 항목입니다.")
    // 정규식 보완: 하이픈이 있든 없든 처리 가능하도록 혹은 입력 폼과 맞춤
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(010-0000-0000)이어야 합니다.")
    private String contact;

    // 2. 장비 정보
    @NotBlank(message = "장비 종류를 선택해주세요.")
    private String cateId;

    @NotBlank(message = "제조사를 입력해주세요.")
    private String vendor;

    @NotBlank(message = "모델명을 입력해주세요.")
    private String modelName;

    @NotNull(message = "장비 높이(Unit)를 입력하거나 선택해주세요.")
    @Min(value = 1, message = "최소 1U 이상이어야 합니다.")
    @Max(value = 52, message = "랙 최대 높이(52U)를 초과할 수 없습니다.")
    private Integer heightUnit;

    @NotNull(message = "예상 소비 전력을 입력해주세요.")
    @PositiveOrZero(message = "소비 전력은 0 이상의 숫자여야 합니다.")
    private Integer powerWatt;

    private String emsStatus = "OFF"; // 기본값 설정

    // 3. 입고 계약 정보
    @NotBlank(message = "입고 목적을 입력해주세요.")
    private String purpose;

    @NotNull(message = "입고 희망일을 선택해주세요.")
    @FutureOrPresent(message = "입고 희망일은 오늘 이후여야 합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // HTML date input과 매핑
    private LocalDate startDate;

    @NotNull(message = "계약 기간을 선택해주세요.")
    private Integer termMonth;

    // ⭐ [추가] 신청자 아이디 (이력 조회용)
    private String memberId;


    public Request toEntity() {
        return Request.builder()
                .memberId(this.memberId)
                .companyName(this.companyName)
                .companyPhone(this.companyPhone)
                .userName(this.userName)
                .contact(this.contact)
                .cateId(this.cateId)
                .vendor(this.vendor)
                .modelName(this.modelName)
                .heightUnit(this.heightUnit)
                .powerWatt(this.powerWatt)
                .emsStatus(this.emsStatus != null ? this.emsStatus : "OFF")
                .status("WAITING") // 초기 상태값 강제 설정
                .purpose(this.purpose)
                .startDate(this.startDate)
                .termMonth(this.termMonth)
                .build();
    }
}