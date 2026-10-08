package com.example.farmerbackend.controller;

import com.example.farmerbackend.entity.Lead;
import com.example.farmerbackend.repository.LeadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadRepository leadRepository;

    public LeadController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @PostMapping
    public ResponseEntity<?> createLead(@RequestBody Lead lead) {
        try {
            Lead saved = leadRepository.save(lead);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<?> getFarmerLeads(@PathVariable Long farmerId) {
        return ResponseEntity.ok(leadRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<?> getBuyerLeads(@PathVariable Long buyerId) {
        return ResponseEntity.ok(leadRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return leadRepository.findById(id).map(lead -> {
            lead.setStatus(status);
            leadRepository.save(lead);
            return ResponseEntity.ok(lead);
        }).orElse(ResponseEntity.notFound().build());
    }
}
