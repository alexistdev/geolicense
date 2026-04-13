package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = DatabaseTableNames.TB_PRODUCT)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_PRODUCT + " SET is_deleted = true WHERE uuid = ?")
@SQLRestriction("is_deleted = false")
public class Product extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false   )
    private String name;

    @NotBlank
    @Size(max = 10)
    @Column(length = 10, nullable = false   )
    private String version;

    @Nullable
    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false   )
    private String sku;

    @NotNull
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;


}
