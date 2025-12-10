package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.service.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/racks")
@RequiredArgsConstructor
public class RackController {
    private final RackService rackService;

    // 전체 Rack 조회
    @GetMapping
    public List<Rack> getAllRacks() {
        return  rackService.findAllRacks();
    }

    // Rack 조회
    @GetMapping("/{id}")
    public ResponseEntity<Rack> getRackByid(@PathVariable("id") Long id) {
        try{
            Rack rack = rackService.findByIdOrThrow(id);
            return ResponseEntity.ok(rack);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Rack 추가
    @PostMapping
    public Rack createRack(@RequestBody Rack rack) {
        return rackService.addRack(rack);
    }

    // Rack 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Rack> patchRack(@PathVariable Long id,
    @RequestBody Rack patch) {
        Rack updated = rackService.updateRackPartially(id, patch);
        if(updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Rack 정보 삭제
    @DeleteMapping("/{id}")
    public void deleteRack(@PathVariable Long id) {
        rackService.deleteRack(id);
    }

}
