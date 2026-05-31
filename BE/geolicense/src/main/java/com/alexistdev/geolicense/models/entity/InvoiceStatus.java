/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import java.util.Set;

public enum InvoiceStatus {
    UNPAID {
        @Override
        public Set<InvoiceStatus> allowedTransitions() {
            return Set.of(AWAITING_VERIFICATION, PAID, CANCELLED);
        }
    },
    AWAITING_VERIFICATION {
        @Override
        public Set<InvoiceStatus> allowedTransitions() {
            return Set.of(PAID, UNPAID);
        }
    },
    PAID {
        @Override
        public Set<InvoiceStatus> allowedTransitions() {
            return Set.of();
        }
    },
    CANCELLED {
        @Override
        public Set<InvoiceStatus> allowedTransitions() {
            return Set.of();
        }
    };

    public abstract Set<InvoiceStatus> allowedTransitions();

    public boolean canTransitionTo(InvoiceStatus next) {
        return allowedTransitions().contains(next);
    }
}
