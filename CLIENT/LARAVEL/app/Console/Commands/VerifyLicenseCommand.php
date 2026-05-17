<?php

namespace App\Console\Commands;

use App\Services\GeoLicense\LicenseVerificationService;
use Illuminate\Console\Command;

class VerifyLicenseCommand extends Command
{
    protected $signature   = 'geolicense:verify';
    protected $description = 'Verify the application license with the GeoLicense server';

    public function handle(LicenseVerificationService $service): int
    {
        $this->info('Verifying license...');

        $service->verify();

        $this->info('Done.');

        return Command::SUCCESS;
    }
}
