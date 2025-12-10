// 장비 요청

package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "DC_REQUEST")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "REQ_SEQ_GEN", sequenceName = "SEQ_REQ_ID", allocationSize = 1)

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REQ_SEQ_GEN")
    @Column(name = "REQ_ID")
    private Long id;            // 요청 ID

    @Column(name = "USER_NAME", nullable = false)
    private String userName;    // 요청자 이름

    @Column(name = "CONTACT")
    private String contact;     // 연락처

    @Column(name = "CATE_ID")
    private String cateId;      // 장비 종류 (Category ID)

    @Column(name = "VENDOR")
    private String vendor;      // 제조사

    @Column(name = "MODEL_NAME")
    private String modelName;   // 모델명

    @Column(name = "HEIGHT_UNIT", nullable = false)
    private Integer heightUnit; // 2U, 4U 등

    @Column(name = "STATUS")
    private String status;      // WAITING, APPROVED

    @CreatedDate
    @Column(name = "REQ_DATE", updatable = false)
    private LocalDateTime reqDate;  // 요청일자

}
