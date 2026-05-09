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
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(
        name = DatabaseTableNames.TB_ROLE_MENUS,
        uniqueConstraints = {
                @UniqueConstraint(name="uk_role_menu",columnNames = {"role_id", "menu_uuid"})
        },
        indexes = {
                @Index(name="idx_role_menu_role_id",columnList = "role_id"),
                @Index(name="idx_role_menu_menu_id",columnList = "menu_uuid")
        }
)
public class RoleMenu extends BaseEntity<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role_id",nullable = false, length = 50)
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_uuid", nullable = false)
    private Menu menu;
}
