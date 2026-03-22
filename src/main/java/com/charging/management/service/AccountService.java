package com.charging.management.service;

import com.charging.management.dto.AccountDTO;
import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.Account;
import com.charging.management.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public ApiResponse<AccountDTO> getAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultAccount(userId));

        return ApiResponse.success(toDTO(account));
    }

    @Transactional
    public ApiResponse<AccountDTO> recharge(Long userId, BigDecimal amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultAccount(userId));

        if (!"ACTIVE".equals(account.getStatus())) {
            return ApiResponse.error(400, "账户状态异常，无法充值");
        }

        account.setBalance(account.getBalance().add(amount));
        account.setTotalRecharge(account.getTotalRecharge().add(amount));
        account = accountRepository.save(account);

        return ApiResponse.success("充值成功", toDTO(account));
    }

    @Transactional
    public ApiResponse<AccountDTO> deduct(Long userId, BigDecimal amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElse(null);

        if (account == null) {
            return ApiResponse.error(404, "账户不存在");
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            return ApiResponse.error(400, "账户状态异常");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            return ApiResponse.error(400, "余额不足");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalConsumption(account.getTotalConsumption().add(amount));
        account = accountRepository.save(account);

        return ApiResponse.success("扣款成功", toDTO(account));
    }

    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        Account account = accountRepository.findByUserId(userId).orElse(null);
        if (account == null || !"ACTIVE".equals(account.getStatus())) {
            return false;
        }
        return account.getBalance().compareTo(amount) >= 0;
    }

    private Account createDefaultAccount(Long userId) {
        Account account = Account.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .totalRecharge(BigDecimal.ZERO)
                .totalConsumption(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        return accountRepository.save(account);
    }

    private AccountDTO toDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .totalRecharge(account.getTotalRecharge())
                .totalConsumption(account.getTotalConsumption())
                .status(account.getStatus())
                .createTime(account.getCreateTime())
                .updateTime(account.getUpdateTime())
                .build();
    }
}
