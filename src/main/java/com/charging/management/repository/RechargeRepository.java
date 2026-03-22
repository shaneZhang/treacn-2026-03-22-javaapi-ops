package com.charging.management.repository;

import com.charging.management.entity.Recharge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RechargeRepository extends JpaRepository<Recharge, Long> {

    Optional<Recharge> findByOrderNo(String orderNo);

    Page<Recharge> findByUserId(Long userId, Pageable pageable);

    Page<Recharge> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("SELECT r FROM Recharge r WHERE r.userId = :userId AND r.createTime BETWEEN :startTime AND :endTime")
    Page<Recharge> findByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
