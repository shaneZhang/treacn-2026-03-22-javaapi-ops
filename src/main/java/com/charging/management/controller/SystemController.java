package com.charging.management.controller;

import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.OperationLog;
import com.charging.management.entity.Permission;
import com.charging.management.entity.Role;
import com.charging.management.entity.User;
import com.charging.management.repository.UserRepository;
import com.charging.management.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final UserRepository userRepository;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK", "Service is healthy");
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Role>> getAllRoles() {
        return systemService.getAllRoles();
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> getRoleById(@PathVariable Long id) {
        return systemService.getRoleById(id);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> createRole(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        return systemService.createRole(code, name, description);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> updateRole(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean enabled) {
        return systemService.updateRole(id, name, description, enabled);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        return systemService.deleteRole(id);
    }

    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds) {
        return systemService.assignPermissions(roleId, permissionIds);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Permission>> getAllPermissions() {
        return systemService.getAllPermissions();
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Permission> createPermission(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String uri,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String description) {
        return systemService.createPermission(code, name, type, uri, method, description);
    }

    @DeleteMapping("/permissions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        return systemService.deletePermission(id);
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> assignRoles(
            @PathVariable Long userId,
            @RequestBody Set<Long> roleIds) {
        return systemService.assignRoles(userId, roleIds);
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return systemService.getOperationLogs(pageable);
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OperationLog>> getOperationLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return systemService.getOperationLogsByUser(userId, pageable);
    }
}
