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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_PAYMENT)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_PAYMENT + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Payment extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", referencedColumnName = "id")
    private BankAccount bankAccount;

    @Size(max = 100)
    @Column(name = "snapshot_bank_name", length = 100)
    private String snapshotBankName;

    @Size(max = 50)
    @Column(name = "snapshot_account_number", length = 50)
    private String snapshotAccountNumber;

    @Size(max = 100)
    @Column(name = "snapshot_account_holder", length = 100)
    private String snapshotAccountHolder;

    @NotBlank
    @Size(max = 255)
    @Column(name="provider", nullable = false)
    private String provider;

    @NotBlank
    @Size(max = 255)
    @Column(name="provider_reference", nullable = false)
    private String providerReference;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 3)
    @Column(name="currency",nullable = false)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 50)
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    @Column(name="paid_at", nullable = false)
    protected LocalDateTime paidAt;
}
