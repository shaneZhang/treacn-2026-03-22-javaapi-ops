package com.charging.management.service;

import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.Account;
import com.charging.management.entity.Recharge;
import com.charging.management.repository.AccountRepository;
import com.charging.management.repository.RechargeRepository;
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
public class RechargeService {

    private final RechargeRepository rechargeRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Transactional
    public ApiResponse<Recharge> createRecharge(Long userId, BigDecimal amount, String paymentMethod, String remark) {
        String orderNo = generateOrderNo();

        Recharge recharge = Recharge.builder()
                .orderNo(orderNo)
                .userId(userId)
                .amount(amount)
                .status("PENDING")
                .paymentMethod(paymentMethod)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        recharge = rechargeRepository.save(recharge);

        return ApiResponse.success("充值订单创建成功", recharge);
    }

    @Transactional
    public ApiResponse<Recharge> confirmRecharge(String orderNo) {
        Recharge recharge = rechargeRepository.findByOrderNo(orderNo)
                .orElse(null);

        if (recharge == null) {
            return ApiResponse.error(404, "充值订单不存在");
        }

        if (!"PENDING".equals(recharge.getStatus())) {
            return ApiResponse.error(400, "订单状态不允许确认");
        }

        recharge.setStatus("COMPLETED");
        recharge.setTransactionId(UUID.randomUUID().toString());
        recharge.setUpdateTime(LocalDateTime.now());
        recharge = rechargeRepository.save(recharge);

        Account account = accountRepository.findByUserId(recharge.getUserId())
                .orElse(null);

        if (account != null) {
            account.setBalance(account.getBalance().add(recharge.getAmount()));
            account.setTotalRecharge(account.getTotalRecharge().add(recharge.getAmount()));
            accountRepository.save(account);
        }

        return ApiResponse.success("充值确认成功", recharge);
    }

    @Transactional
    public ApiResponse<Recharge> cancelRecharge(String orderNo) {
        Recharge recharge = rechargeRepository.findByOrderNo(orderNo)
                .orElse(null);

        if (recharge == null) {
            return ApiResponse.error(404, "充值订单不存在");
        }

        if (!"PENDING".equals(recharge.getStatus())) {
            return ApiResponse.error(400, "订单状态不允许取消");
        }

        recharge.setStatus("CANCELLED");
        recharge.setUpdateTime(LocalDateTime.now());
        recharge = rechargeRepository.save(recharge);

        return ApiResponse.success("充值取消成功", recharge);
    }

    public ApiResponse<Page<Recharge>> getRechargeList(Long userId, Pageable pageable) {
        Page<Recharge> page = rechargeRepository.findByUserId(userId, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Page<Recharge>> getRechargeListByStatus(Long userId, String status, Pageable pageable) {
        Page<Recharge> page = rechargeRepository.findByUserIdAndStatus(userId, status, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Page<Recharge>> getRechargeListByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        Page<Recharge> page = rechargeRepository.findByUserIdAndTimeRange(userId, startTime, endTime, pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Recharge> getRechargeByOrderNo(String orderNo) {
        Recharge recharge = rechargeRepository.findByOrderNo(orderNo)
                .orElse(null);

        if (recharge == null) {
            return ApiResponse.error(404, "充值订单不存在");
        }

        return ApiResponse.success(recharge);
    }

    private String generateOrderNo() {
        return "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
