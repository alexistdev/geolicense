/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.exceptions;

public class LicenseExpiredException extends RuntimeException {
    public LicenseExpiredException(String message) {
        super(message);
    }
}
