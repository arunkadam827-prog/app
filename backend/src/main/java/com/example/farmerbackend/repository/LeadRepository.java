package com.example.farmerbackend.repository;

import com.example.farmerbackend.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
    List<Lead> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
