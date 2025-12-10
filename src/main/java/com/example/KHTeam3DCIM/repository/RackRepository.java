package com.example.KHTeam3DCIM.repository;

import com.example.KHTeam3DCIM.domain.Rack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RackRepository extends JpaRepository<Rack,Long> {
    Optional<Rack> findByRackName(String rackName);
}
