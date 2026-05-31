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
@Table(name = DatabaseTableNames.TB_INVOICE)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_INVOICE + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Invoice extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Orders orders;

    @NotBlank
    @Size(max = 255)
    @Column(name="invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 3)
    @Column(name="currency",nullable = false)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 50)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @NotNull
    @Column(name="issued_at", nullable = false)
    protected LocalDateTime issuedAt;

    @NotNull
    @Column(name = "unique_code", nullable = false)
    private Integer uniqueCode;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "discount", nullable = false, precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0")
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "tax", nullable = false, precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0")
    private BigDecimal tax = BigDecimal.ZERO;
}
