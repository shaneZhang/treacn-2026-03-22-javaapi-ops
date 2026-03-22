package com.charging.management.service;

import com.charging.management.dto.ApiResponse;
import com.charging.management.entity.OperationLog;
import com.charging.management.entity.Permission;
import com.charging.management.entity.Role;
import com.charging.management.entity.User;
import com.charging.management.repository.OperationLogRepository;
import com.charging.management.repository.PermissionRepository;
import com.charging.management.repository.RoleRepository;
import com.charging.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final OperationLogRepository operationLogRepository;

    public ApiResponse<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ApiResponse.success(roles);
    }

    public ApiResponse<Role> getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ApiResponse.error(404, "角色不存在");
        }
        return ApiResponse.success(role);
    }

    @Transactional
    public ApiResponse<Role> createRole(String code, String name, String description) {
        if (roleRepository.existsByCode(code)) {
            return ApiResponse.error(400, "角色编码已存在");
        }

        Role role = Role.builder()
                .code(code)
                .name(name)
                .description(description)
                .enabled(true)
                .build();

        role = roleRepository.save(role);
        return ApiResponse.success("角色创建成功", role);
    }

    @Transactional
    public ApiResponse<Role> updateRole(Long id, String name, String description, Boolean enabled) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ApiResponse.error(404, "角色不存在");
        }

        if (name != null) {
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }
        if (enabled != null) {
            role.setEnabled(enabled);
        }

        role = roleRepository.save(role);
        return ApiResponse.success("角色更新成功", role);
    }

    @Transactional
    public ApiResponse<Void> deleteRole(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ApiResponse.error(404, "角色不存在");
        }

        roleRepository.delete(role);
        return ApiResponse.<Void>success("角色删除成功", null);
    }

    @Transactional
    public ApiResponse<Role> assignPermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return ApiResponse.error(404, "角色不存在");
        }

        Set<Permission> permissions = new java.util.HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);

        role = roleRepository.save(role);
        return ApiResponse.success("权限分配成功", role);
    }

    public ApiResponse<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return ApiResponse.success(permissions);
    }

    @Transactional
    public ApiResponse<Permission> createPermission(String code, String name, String type, String uri, String method, String description) {
        if (permissionRepository.existsByCode(code)) {
            return ApiResponse.error(400, "权限编码已存在");
        }

        Permission permission = Permission.builder()
                .code(code)
                .name(name)
                .type(type)
                .uri(uri)
                .method(method)
                .description(description)
                .enabled(true)
                .build();

        permission = permissionRepository.save(permission);
        return ApiResponse.success("权限创建成功", permission);
    }

    @Transactional
    public ApiResponse<Void> deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            return ApiResponse.error(404, "权限不存在");
        }

        permissionRepository.delete(permission);
        return ApiResponse.<Void>success("权限删除成功", null);
    }

    @Transactional
    public ApiResponse<User> assignRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        Set<Role> roles = new java.util.HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);

        user = userRepository.save(user);
        return ApiResponse.success("角色分配成功", user);
    }

    public ApiResponse<Page<OperationLog>> getOperationLogs(Pageable pageable) {
        Page<OperationLog> page = operationLogRepository.findAll(pageable);
        return ApiResponse.success(page);
    }

    public ApiResponse<Page<OperationLog>> getOperationLogsByUser(Long userId, Pageable pageable) {
        Page<OperationLog> page = operationLogRepository.findByUserId(userId, pageable);
        return ApiResponse.success(page);
    }

    @Transactional
    public void saveOperationLog(Long userId, String username, String operation, String method, String params, String ip, String location) {
        OperationLog log = OperationLog.builder()
                .userId(userId)
                .username(username)
                .operation(operation)
                .method(method)
                .params(params)
                .ip(ip)
                .location(location)
                .build();

        operationLogRepository.save(log);
    }
}
