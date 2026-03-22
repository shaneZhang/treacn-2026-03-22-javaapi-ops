package com.charging.management.service;

import com.charging.management.dto.*;
import com.charging.management.entity.Account;
import com.charging.management.entity.Role;
import com.charging.management.entity.User;
import com.charging.management.repository.AccountRepository;
import com.charging.management.repository.RoleRepository;
import com.charging.management.repository.UserRepository;
import com.charging.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public ApiResponse<LoginResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error(400, "用户名已存在");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error(400, "邮箱已被注册");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return ApiResponse.error(400, "手机号已被注册");
        }

        Role defaultRole = roleRepository.findByCode("USER")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .code("USER")
                            .name("普通用户")
                            .description("普通用户角色")
                            .enabled(true)
                            .build();
                    return roleRepository.save(role);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        Account account = Account.builder()
                .userId(user.getId())
                .balance(java.math.BigDecimal.ZERO)
                .totalRecharge(java.math.BigDecimal.ZERO)
                .totalConsumption(java.math.BigDecimal.ZERO)
                .status("ACTIVE")
                .build();
        accountRepository.save(account);

        return ApiResponse.success("注册成功", buildLoginResponse(user));
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsernameWithRoles(request.getUsername())
                    .orElse(null);

            if (user == null) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            if (!user.getEnabled()) {
                return ApiResponse.error(403, "账户已被禁用");
            }

            if (!user.getAccountNonLocked()) {
                return ApiResponse.error(403, "账户已被锁定");
            }

            String token = jwtTokenProvider.generateToken(authentication);
            return ApiResponse.success("登录成功", buildLoginResponse(user, token));

        } catch (Exception e) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
    }

    public ApiResponse<UserInfo> getCurrentUser(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElse(null);

        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        return ApiResponse.success(buildUserInfo(user));
    }

    public ApiResponse<Void> logout(String username) {
        return ApiResponse.<Void>success("退出登录成功", null);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getUsername());
        return buildLoginResponse(user, token);
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(buildUserInfo(user))
                .build();
    }

    private UserInfo buildUserInfo(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toSet());

        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .createTime(user.getCreateTime())
                .build();
    }
}
