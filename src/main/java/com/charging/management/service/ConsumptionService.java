package com.charging.management.service;

import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.Consumption;
import com.charging.management.repository.ConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final ConsumptionRepository consumptionRepository;
    private final AccountService accountService;

    @Transactional
    public ApiResponse<Consumption> createConsumption(Long userId, String serviceType, BigDecimal amount, String description, String relatedOrderNo) {
        if (!accountService.hasEnoughBalance(userId, amount)) {
            return ApiResponse.error(400, "余额不足");
        }

        String orderNo = generateOrderNo();

        Consumption consumption = Consumption.builder()
                .orderNo(orderNo)
                .userId(userId)
                .serviceType(serviceType)
                .amount(amount)
                .status("COMPLETED")
                .description(description)
                .relatedOrderNo(relatedOrderNo)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        consumption = consumptionRepository.save(consumption);

        accountService.deduct(userId, amount);

        return ApiResponse.success("消费成功", consumption);
    }

    public ApiResponse<Page<Consumption>> getConsumptionList(Long userId, Pageable pageable) {
        Page<Consumption> page = consumptionRepository.findByUserId(userId, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Page<Consumption>> getConsumptionListByServiceType(Long userId, String serviceType, Pageable pageable) {
        Page<Consumption> page = consumptionRepository.findByUserIdAndServiceType(userId, serviceType, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Page<Consumption>> getConsumptionListByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        Page<Consumption> page = consumptionRepository.findByUserIdAndTimeRange(userId, startTime, endTime, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Consumption> getConsumptionByOrderNo(String orderNo) {
        Consumption consumption = consumptionRepository.findByOrderNo(orderNo)
                .orElse(null);

        if (consumption == null) {
            return ApiResponse.error(404, "消费订单不存在");
        }

        return ApiResponse.success(consumption);
    }

    private String generateOrderNo() {
        return "CS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
