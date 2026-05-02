/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = DatabaseTableNames.TB_AUDIT_LOG)
public class AuditLog extends BaseEntity<String>{

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "license_id", nullable = false)
    private UUID licenseId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLogAction action;

    @Column(length = 255)
    private String reason;

    @Column(name = "ip_address", length = 255)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

}
