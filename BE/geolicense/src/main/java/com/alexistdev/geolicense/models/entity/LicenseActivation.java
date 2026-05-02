/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(
        name = DatabaseTableNames.TB_LICENSE_ACTIVATION,
        uniqueConstraints = @UniqueConstraint(
                name = "uq_license_machine",
                columnNames = {"license_id", "machine_id"}
        )
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_LICENSE_ACTIVATION + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class LicenseActivation extends BaseEntity<String>{

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private License license;

    @NotNull
    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Nullable
    @Column(name = "os_info")
    private String osInfo;

    @Column(name = "activated_at", nullable = false)
    private LocalDateTime activatedAt;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "is_activated", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActivated = true;
}
