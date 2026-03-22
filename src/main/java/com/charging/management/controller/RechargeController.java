package com.charging.management.controller;

import com.charging.management.dto.ApiResponse;
import com.charging.management.dto.RechargeRequest;
import com.charging.management.entity.Recharge;
import com.charging.management.entity.User;
import com.charging.management.repository.UserRepository;
import com.charging.management.service.RechargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeService rechargeService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<Recharge> createRecharge(
            @Valid @RequestBody RechargeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails.getUsername());
        return rechargeService.createRecharge(userId, request.getAmount(), request.getPaymentMethod(), request.getRemark());
    }

    @PostMapping("/confirm/{orderNo}")
    public ApiResponse<Recharge> confirmRecharge(@PathVariable String orderNo) {
        return rechargeService.confirmRecharge(orderNo);
    }

    @PostMapping("/cancel/{orderNo}")
    public ApiResponse<Recharge> cancelRecharge(@PathVariable String orderNo) {
        return rechargeService.cancelRecharge(orderNo);
    }

    @GetMapping
    public ApiResponse<Page<Recharge>> getRechargeList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Long userId = getUserId(userDetails.getUsername());
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return rechargeService.getRechargeList(userId, pageable);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<Page<Recharge>> getRechargeListByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return rechargeService.getRechargeListByStatus(userId, status, pageable);
    }

    @GetMapping("/timeRange")
    public ApiResponse<Page<Recharge>> getRechargeListByTimeRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return rechargeService.getRechargeListByTimeRange(userId, startTime, endTime, pageable);
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Recharge> getRechargeByOrderNo(@PathVariable String orderNo) {
        return rechargeService.getRechargeByOrderNo(orderNo);
    }

    private Long getUserId(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }
}
