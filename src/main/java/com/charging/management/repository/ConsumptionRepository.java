package com.charging.management.repository;

import com.charging.management.entity.Consumption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConsumptionRepository extends JpaRepository<Consumption, Long> {

    Optional<Consumption> findByOrderNo(String orderNo);

    Page<Consumption> findByUserId(Long userId, Pageable pageable);

    Page<Consumption> findByUserIdAndServiceType(Long userId, String serviceType, Pageable pageable);

    @Query("SELECT c FROM Consumption c WHERE c.userId = :userId AND c.createTime BETWEEN :startTime AND :endTime")
    Page<Consumption> findByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
