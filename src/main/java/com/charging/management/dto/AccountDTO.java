package com.charging.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {

    private Long id;

    private Long userId;

    private BigDecimal balance;

    private BigDecimal totalRecharge;

    private BigDecimal totalConsumption;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
