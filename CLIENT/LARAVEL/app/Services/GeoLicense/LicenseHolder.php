<?php

namespace App\Services\GeoLicense;

use Illuminate\Support\Facades\Cache;

/**
 * Persists license state in the Laravel cache (Redis or file driver).
 *
 * This replaces the in-memory singleton used by the Spring Boot starter.
 * All values are stored with Cache::forever() so they survive PHP restarts;
 * validity is re-evaluated on every scheduled verification run.
 */
class LicenseHolder
{
    private const TOKEN_KEY      = 'geolicense.token';
    private const VALID_KEY      = 'geolicense.valid';
    private const LAST_VALID_KEY = 'geolicense.last_valid_at';

    public function setToken(string $token): void
    {
        Cache::forever(self::TOKEN_KEY, $token);
    }

    public function getToken(): ?string
    {
        return Cache::get(self::TOKEN_KEY);
    }

    public function setValid(bool $valid): void
    {
        Cache::forever(self::VALID_KEY, $valid);

        if ($valid) {
            Cache::forever(self::LAST_VALID_KEY, now()->timestamp);
        }
    }

    public function isValid(): bool
    {
        return (bool) Cache::get(self::VALID_KEY, false);
    }

    /**
     * Returns a Unix timestamp of the last successful verification, or null if never verified.
     */
    public function getLastValidAt(): ?int
    {
        return Cache::get(self::LAST_VALID_KEY);
    }

    /**
     * Wipe all cached license state (useful for forced re-activation).
     */
    public function forget(): void
    {
        Cache::forget(self::TOKEN_KEY);
        Cache::forget(self::VALID_KEY);
        Cache::forget(self::LAST_VALID_KEY);
    }
}
