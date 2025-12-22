package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.DcimEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DcimEnvironmentRepository extends JpaRepository<DcimEnvironment, Long> {
    // ID가 1인 설정을 주로 쓰므로 기본 메서드로 충분합니다.
}