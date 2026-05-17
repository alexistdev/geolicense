<?php

namespace App\Services\GeoLicense;

use Illuminate\Support\Str;

class MachineIdGenerator
{
    public function generate(): string
    {
        try {
            $raw = $this->getMachineFingerprint() . '|' . (gethostname() ?: 'unknown-host');
            return hash('sha256', $raw);
        } catch (\Throwable) {
            return $this->getOrCreateFileBasedId();
        }
    }

    private function getMachineFingerprint(): string
    {
        // Linux — most reliable on servers and Docker with a persistent volume
        if (is_readable('/etc/machine-id')) {
            $id = trim((string) file_get_contents('/etc/machine-id'));
            if ($id !== '') {
                return $id;
            }
        }

        // macOS
        if (PHP_OS_FAMILY === 'Darwin') {
            $uuid = shell_exec("ioreg -rd1 -c IOPlatformExpertDevice | awk '/IOPlatformUUID/ {print $3}'");
            if ($uuid) {
                return trim($uuid, " \"\n\r");
            }
        }

        // Windows
        if (PHP_OS_FAMILY === 'Windows') {
            $output = shell_exec('wmic csproduct get UUID 2>nul');
            if ($output) {
                $lines = array_filter(array_map('trim', explode("\n", $output)));
                // skip the "UUID" header line
                $values = array_values(array_filter($lines, fn ($l) => $l !== 'UUID'));
                if (!empty($values)) {
                    return $values[0];
                }
            }
        }

        throw new \RuntimeException('Cannot determine machine fingerprint on ' . PHP_OS_FAMILY);
    }

    private function getOrCreateFileBasedId(): string
    {
        $path = storage_path('.geolicense-machine-id');

        if (file_exists($path)) {
            return trim((string) file_get_contents($path));
        }

        $id = str_replace('-', '', (string) Str::uuid());
        file_put_contents($path, $id);

        return $id;
    }
}
