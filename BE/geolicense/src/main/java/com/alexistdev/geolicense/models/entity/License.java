package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_LICENSE)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_LICENSE + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class License extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_type_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LicenseType licenseType;

    @NotBlank
    @Column(name = "license_key" , nullable = false, unique = true)
    private String licenseKey;

    @NotNull
    @Column(name="used_seats", nullable = false)
    private int usedSeats;

    @NotNull
    @Column(name="issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @NotNull
    @Column(name="expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
