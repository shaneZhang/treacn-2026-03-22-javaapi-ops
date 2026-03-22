package com.charging.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_operation_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String operation;

    @Column(length = 100)
    private String method;

    @Column(length = 500)
    private String params;

    @Column(length = 50)
    private String ip;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;
}
