<?php

namespace App\Services\GeoLicense;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class LicenseVerificationService
{
    public function __construct(
        private readonly LicenseHolder      $holder,
        private readonly MachineIdGenerator $machineId,
    ) {}

    /**
     * Calls POST /api/v1/licenses/verify on the GeoLicense server.
     * Updates the cache validity flag based on the response.
     *
     * If the server is unreachable, validity is left unchanged so the
     * grace period in LicenseValidationMiddleware can absorb the outage.
     */
    public function verify(): void
    {
        $token = $this->holder->getToken();

        if (empty($token)) {
            Log::warning('GeoLicense: verification skipped — no token in cache.');
            $this->holder->setValid(false);
            return;
        }

        $serverUrl = config('geolicense.server_url');

        try {
            $response = Http::timeout(15)
                ->acceptJson()
                ->post("{$serverUrl}/api/v1/licenses/verify", [
                    'token'     => $token,
                    'machineId' => $this->machineId->generate(),
                ]);

            $valid = $response->successful() && $response->json('status');
            $this->holder->setValid($valid);

            if (!$valid) {
                $message = $response->json('messages.0', 'unknown reason');
                Log::warning("GeoLicense: verification failed — {$message}");
            } else {
                Log::debug('GeoLicense: license verified successfully.');
            }
        } catch (\Throwable $e) {
            // Do not flip valid to false — let the grace period handle transient outages.
            Log::warning('GeoLicense: server unreachable during verification.', [
                'error' => $e->getMessage(),
            ]);
        }
    }
}
