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
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_ORDER_ITEM)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_ORDER_ITEM + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class OrderItem extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Orders orders;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_plan_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LicensePlan licensePlan;

    @NotNull
    @Column(name="quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int quantity;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private double unitPrice;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private double totalPrice;

}
