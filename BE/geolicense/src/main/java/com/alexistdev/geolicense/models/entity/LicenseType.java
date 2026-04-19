package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@DynamicUpdate
@Table(name = DatabaseTableNames.TB_LICENSE_TYPE)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_LICENSE_TYPE + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class LicenseType extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Nullable
    @Size(max = 255)
    private String description;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private int duration_days;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 5")
    private int max_seats;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean is_trial = false;
}
