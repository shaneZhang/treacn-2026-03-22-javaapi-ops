package com.charging.management.controller;

import com.charging.management.dto.ApiResponse;
import com.charging.management.dto.ConsumptionRequest;
import com.charging.management.entity.Consumption;
import com.charging.management.entity.User;
import com.charging.management.repository.UserRepository;
import com.charging.management.service.ConsumptionService;
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
@RequestMapping("/api/consumption")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<Consumption> createConsumption(
            @Valid @RequestBody ConsumptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails.getUsername());
        return consumptionService.createConsumption(
                userId,
                request.getServiceType(),
                request.getAmount(),
                request.getDescription(),
                request.getRelatedOrderNo()
        );
    }

    @GetMapping
    public ApiResponse<Page<Consumption>> getConsumptionList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Long userId = getUserId(userDetails.getUsername());
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return consumptionService.getConsumptionList(userId, pageable);
    }

    @GetMapping("/serviceType/{serviceType}")
    public ApiResponse<Page<Consumption>> getConsumptionListByServiceType(
            @PathVariable String serviceType,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return consumptionService.getConsumptionListByServiceType(userId, serviceType, pageable);
    }

    @GetMapping("/timeRange")
    public ApiResponse<Page<Consumption>> getConsumptionListByTimeRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return consumptionService.getConsumptionListByTimeRange(userId, startTime, endTime, pageable);
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<Consumption> getConsumptionByOrderNo(@PathVariable String orderNo) {
        return consumptionService.getConsumptionByOrderNo(orderNo);
    }

    private Long getUserId(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }
}
