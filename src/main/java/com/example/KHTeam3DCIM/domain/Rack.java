package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DC_RACK")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rack {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rack_seq")
    @SequenceGenerator(name = "rack_seq", sequenceName = "SEQ_RACK_ID", allocationSize = 1)
    @Column(name = "RACK_ID")
    private Long rackId;

    @Column(name = "RACK_NAME", nullable = false, length = 50)
    private String rackName;

    @Column(name = "TOTAL_UNIT", nullable = false)
    @Builder.Default
    private Long totalUnit = 42L;

    @Column(name = "LOCATION_DESC", length = 200)
    private String locationDesc;

    @Builder
    public Rack(String rackName, int totalUnit, String locationDesc) {
        this.rackName = rackName;
        this.totalUnit = (long) totalUnit;
        this.locationDesc = locationDesc;
    }
}
