/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import java.util.Set;

public enum LicenseStatus {
    ACTIVE {
        @Override
        public Set<LicenseStatus> allowedTransitions() {
            return Set.of(SUSPENDED, REVOKED);
        }
    },
    SUSPENDED {
        @Override
        public Set<LicenseStatus> allowedTransitions() {
            return Set.of(ACTIVE, REVOKED);
        }
    },
    REVOKED {
        @Override
        public Set<LicenseStatus> allowedTransitions() {
            return Set.of();
        }
    };

    public abstract Set<LicenseStatus> allowedTransitions();

    public boolean canTransitionTo(LicenseStatus next) {
        return allowedTransitions().contains(next);
    }
}
