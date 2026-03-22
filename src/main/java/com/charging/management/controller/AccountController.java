package com.charging.management.controller;

import com.charging.management.dto.AccountDTO;
import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.User;
import com.charging.management.repository.UserRepository;
import com.charging.management.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<AccountDTO> getAccount(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails.getUsername());
        return accountService.getAccount(userId);
    }

    private Long getUserId(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }
}
