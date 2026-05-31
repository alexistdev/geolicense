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
@Table(
    name = DatabaseTableNames.TB_PAYMENT_GATEWAY_CONFIG,
    uniqueConstraints = @UniqueConstraint(columnNames = "payment_method_id")
)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_PAYMENT_GATEWAY_CONFIG + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class PaymentGatewayConfig extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PaymentMethod paymentMethod;

    @NotBlank
    @Size(max = 500)
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @NotBlank
    @Size(max = 500)
    @Column(name = "webhook_token", nullable = false, length = 500)
    private String webhookToken;

    @Column(name = "extra_config", columnDefinition = "TEXT")
    private String extraConfig;
}
