package com.alexistdev.geolicense.models.entity;

import com.alexistdev.geolicense.config.DatabaseTableNames;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = DatabaseTableNames.TB_USERS)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_USERS + " SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NullMarked
public class User extends BaseEntity<String> implements UserDetails {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 150, nullable = false)
    private String fullName;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(length = 200)
    private String password;

    @Enumerated(EnumType.STRING)
    @Nullable
    private Role role;

    @Column(nullable = false)
    private boolean isSuspended = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        return Collections.singletonList(authority);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isSuspended();
    }

    @Override
    public boolean isEnabled() {
        return !isSuspended();
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
