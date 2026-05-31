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
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_BANK_ACCOUNT)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_BANK_ACCOUNT + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class BankAccount extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PaymentMethod paymentMethod;

    @NotBlank
    @Size(max = 100)
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @NotBlank
    @Size(max = 100)
    @Column(name = "account_holder", nullable = false, length = 100)
    private String accountHolder;

    @NotNull
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = Boolean.FALSE;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
