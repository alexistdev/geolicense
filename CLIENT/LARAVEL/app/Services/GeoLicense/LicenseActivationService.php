<?php

namespace App\Services\GeoLicense;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class LicenseActivationService
{
    public function __construct(
        private readonly LicenseHolder      $holder,
        private readonly MachineIdGenerator $machineId,
    ) {}

    /**
     * Calls POST /api/v1/licenses/activate on the GeoLicense server.
     * Stores the returned token in the cache on success.
     *
     * @throws \RuntimeException if activation fails or the server is unreachable
     */
    public function activate(): void
    {
        $serverUrl  = config('geolicense.server_url');
        $licenseKey = config('geolicense.license_key');

        if (empty($licenseKey)) {
            throw new \RuntimeException('GeoLicense: GEOLICENSE_LICENSE_KEY is not set.');
        }

        $response = Http::timeout(15)
            ->acceptJson()
            ->post("{$serverUrl}/api/v1/licenses/activate", [
                'licenseKey' => $licenseKey,
                'machineId'  => $this->machineId->generate(),
                'osInfo'     => php_uname('s') . ' ' . php_uname('r'),
            ]);

        if ($response->failed() || !$response->json('status')) {
            $message = $response->json('messages.0', 'activation rejected');
            throw new \RuntimeException("GeoLicense activation failed: {$message}");
        }

        $token = $response->json('payload.token');

        if (empty($token)) {
            throw new \RuntimeException('GeoLicense: activation response is missing the token.');
        }

        $this->holder->setToken($token);
        $this->holder->setValid(true);

        Log::info('GeoLicense: license activated successfully.', [
            'key_prefix' => substr($licenseKey, 0, 8) . '****',
        ]);
    }
}
