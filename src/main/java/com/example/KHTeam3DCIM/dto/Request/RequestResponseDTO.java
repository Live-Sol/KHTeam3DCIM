package com.example.KHTeam3DCIM.dto.Request;

import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestResponseDTO {

    // [Request 엔티티에서 가져올 정보]
    private Long id;            // 요청 ID
    private String companyName;
    private String companyPhone;
    private String userName;    // 담당자 이름 (요청서 기준)
    private String contact;     // 연락처 (요청서 기준)
    private String email;       // 이메일 (요청서 기준)
    private String vendor;
    private String modelName;
    private Integer heightUnit;
    private Integer powerWatt;
    private String emsStatus;
    private String purpose;
    private LocalDate startDate;
    private Integer termMonth;
    private LocalDateTime reqDate;

    // [Member 엔티티에서 가져올 정보]
    private String memberId;    // 회원 ID
    private String role;        // 권한 (USER, ADMIN)
    private boolean deleted;    // ★ 핵심: 탈퇴 여부 (true/false)

    // 생성자: Request와 Member를 받아서 데이터를 합칩니다.
    public RequestResponseDTO(Request request, Member member) {
        // 1. Request 정보 매핑
        this.id = request.getId();
        this.companyName = request.getCompanyName();
        this.companyPhone = request.getCompanyPhone();
        this.userName = request.getUserName();
        this.contact = request.getContact();
        this.email = request.getEmail();
        this.vendor = request.getVendor();
        this.modelName = request.getModelName();
        this.heightUnit = request.getHeightUnit();
        this.powerWatt = request.getPowerWatt();
        this.emsStatus = request.getEmsStatus();
        this.purpose = request.getPurpose();
        this.startDate = request.getStartDate();
        this.termMonth = request.getTermMonth();
        this.reqDate = request.getReqDate(); // BaseEntity에 있다면

        // 2. Member 정보 매핑
        this.memberId = member.getMemberId();

        // 권한 설정
        this.role = (member.getRole() != null) ? member.getRole().name() : "USER";

        // ★ 탈퇴 여부 설정 (Member 엔티티의 isDeleted 값)
        this.deleted = member.isDeleted();
    }
}