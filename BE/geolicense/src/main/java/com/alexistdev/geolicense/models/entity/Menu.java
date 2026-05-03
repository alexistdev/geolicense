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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = DatabaseTableNames.TB_MENU)
public class Menu extends BaseEntity<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(length = 150, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 150)
    @Column(length = 150, nullable = false)
    private String urlink;

    @NotBlank
    @Size(max = 150)
    @Column(length = 150, nullable = false)
    private String classlink;

    @Nullable
    @Size(max = 50)
    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Nullable
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "type_menu", nullable = false)
    private int typeMenu = 0;

    @NotBlank
    @Size(max = 3)
    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        Menu menu = (Menu) obj;
        return id != null && id.equals(menu.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
