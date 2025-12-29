package com.example.KHTeam3DCIM.dto.device;

import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.domain.Member;
import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DeviceResponseDTO {
    // [Device 정보]
    private Long id;
    private String serialNum;
    private String vendor;
    private String modelName;
    private Integer powerWatt;
    private String emsStatus;
    private Integer startUnit;
    private Integer heightUnit;
    private LocalDate contractDate;
    private Integer contractMonth;
    private String status;
    private LocalDateTime deletedAt;

    // [연관 객체 정보]
    private String rackName;
    private String categoryName;

    // [Member 정보]
    private boolean hasMember;     // 회원 존재 여부
    private String memberId;
    private String userName;
    private String companyName;
    private String companyPhone;
    private String contact;
    private String email;
    private String role;
    private boolean memberDeleted; // ★ 핵심: 탈퇴 여부

    public DeviceResponseDTO(Device device) {
        this.id = device.getId();
        this.serialNum = device.getSerialNum();
        this.vendor = device.getVendor();
        this.modelName = device.getModelName();
        this.powerWatt = device.getPowerWatt();
        this.emsStatus = device.getEmsStatus();
        this.startUnit = device.getStartUnit();
        this.heightUnit = device.getHeightUnit();
        this.contractDate = device.getContractDate();
        this.contractMonth = device.getContractMonth();
        this.status = device.getStatus();
        this.deletedAt = device.getDeletedAt();

        if (device.getRack() != null) {
            this.rackName = device.getRack().getRackName();
        }

        if (device.getCategory() != null) {
            this.categoryName = device.getCategory().getName();
        }

        Member member = device.getMember();
        if (member != null) {
            this.hasMember = true;
            this.memberId = member.getMemberId();
            this.userName = member.getName();
            this.companyName = member.getCompanyName();
            this.companyPhone = member.getCompanyPhone();
            this.contact = member.getContact();
            this.email = member.getEmail();
            this.role = (member.getRole() != null) ? member.getRole().name() : "USER";
            this.memberDeleted = member.isDeleted(); // 탈퇴 여부
        } else {
            this.hasMember = false;
            this.memberDeleted = false;
        }
    }
}