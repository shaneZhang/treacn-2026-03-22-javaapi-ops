package com.charging.management.repository;

import com.charging.management.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    Set<Permission> findByCodeIn(Set<String> codes);

    boolean existsByCode(String code);

    List<Permission> findAllById(Iterable<Long> ids);
}
