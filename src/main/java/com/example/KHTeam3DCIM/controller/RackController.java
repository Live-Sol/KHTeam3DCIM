package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.dto.Rack.RackUpdateRequest;
import com.example.KHTeam3DCIM.service.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/racks")
@RequiredArgsConstructor
public class RackController {
    private final RackService rackService;

    // 1. 전체 랙 조회
    @GetMapping
    public List<RackResponse> getAllRacks() {
        return rackService.findAllRacks();
    }

    // 2. 단일 랙 조회
    @GetMapping("/{id}")
    public ResponseEntity<RackResponse> getRackById(@PathVariable Long id) {
        try {
            RackResponse response = rackService.findRackById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. 랙 생성
    @PostMapping
    public ResponseEntity<RackResponse> createRack(@RequestBody RackCreateRequest request) {
        RackResponse response = rackService.addRack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. 랙 부분 수정
    @PatchMapping("/{id}")
    public ResponseEntity<RackResponse> patchRack(@PathVariable Long id,
                                                  @RequestBody RackUpdateRequest patch) {
        try {
            RackResponse response = rackService.updateRackPartially(id, patch);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. 랙 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRack(@PathVariable Long id) {
        rackService.deleteRack(id);
        return ResponseEntity.noContent().build();
    }

}
