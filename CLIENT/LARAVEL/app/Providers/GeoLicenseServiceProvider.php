<?php

namespace App\Providers;

use App\Services\GeoLicense\LicenseActivationService;
use App\Services\GeoLicense\LicenseHolder;
use App\Services\GeoLicense\MachineIdGenerator;
use Illuminate\Support\ServiceProvider;

/**
 * Registers GeoLicense services and triggers activation on first boot.
 *
 * Laravel 11/12/13 — register this provider in bootstrap/providers.php:
 *
 *   return [
 *       App\Providers\AppServiceProvider::class,
 *       App\Providers\GeoLicenseServiceProvider::class,  // <-- add this
 *   ];
 */
class GeoLicenseServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->singleton(MachineIdGenerator::class);
        $this->app->singleton(LicenseHolder::class);

        $this->app->singleton(LicenseActivationService::class, function ($app) {
            return new LicenseActivationService(
                $app->make(LicenseHolder::class),
                $app->make(MachineIdGenerator::class),
            );
        });

        $this->app->singleton(\App\Services\GeoLicense\LicenseVerificationService::class, function ($app) {
            return new \App\Services\GeoLicense\LicenseVerificationService(
                $app->make(LicenseHolder::class),
                $app->make(MachineIdGenerator::class),
            );
        });
    }

    public function boot(): void
    {
        $holder = $this->app->make(LicenseHolder::class);

        // Only activate when no token is cached yet (first boot or after cache flush).
        if (empty($holder->getToken())) {
            $this->app->make(LicenseActivationService::class)->activate();
        }
    }
}
