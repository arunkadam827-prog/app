package com.example.farmerbackend.repository;

import com.example.farmerbackend.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
 List<CustomerOrder> findByUserUserIdOrderByOrderIdDesc(Long userId);
}