/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoleMenuRepo extends JpaRepository<RoleMenu, UUID> {
    @Query("SELECT rm FROM RoleMenu rm JOIN FETCH rm.menu m WHERE rm.role = :role")
    List<RoleMenu> findByRole(Role role);
}
