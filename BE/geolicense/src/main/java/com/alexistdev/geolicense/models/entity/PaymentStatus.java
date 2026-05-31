/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import java.util.Set;

public enum PaymentStatus {
    PENDING {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return Set.of(VERIFIED, REJECTED);
        }
    },
    VERIFIED {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return Set.of();
        }
    },
    REJECTED {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return Set.of(PENDING);
        }
    };

    public abstract Set<PaymentStatus> allowedTransitions();

    public boolean canTransitionTo(PaymentStatus next) {
        return allowedTransitions().contains(next);
    }
}
