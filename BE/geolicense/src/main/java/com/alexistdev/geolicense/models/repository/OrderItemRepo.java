/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepo extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.licensePlan lp JOIN FETCH lp.product JOIN FETCH lp.licenseType WHERE oi.orders.id = :orderId")
    List<OrderItem> findByOrdersId(@Param("orderId") UUID orderId);
}
