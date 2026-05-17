<?php

namespace App\Http\Middleware;

use App\Services\GeoLicense\LicenseHolder;
use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class LicenseValidationMiddleware
{
    public function __construct(private readonly LicenseHolder $holder) {}

    public function handle(Request $request, Closure $next): Response
    {
        if ($this->isExcluded($request)) {
            return $next($request);
        }

        if ($this->holder->isValid() || $this->isWithinGracePeriod()) {
            return $next($request);
        }

        return response()->json([
            'status'   => false,
            'messages' => ['License invalid or expired. Please contact your administrator.'],
        ], Response::HTTP_SERVICE_UNAVAILABLE);
    }

    private function isExcluded(Request $request): bool
    {
        $excludePaths = config('geolicense.exclude_paths', []);

        foreach ($excludePaths as $pattern) {
            if (Str::is($pattern, $request->path())) {
                return true;
            }
        }

        return false;
    }

    private function isWithinGracePeriod(): bool
    {
        $lastValidAt = $this->holder->getLastValidAt();

        if ($lastValidAt === null) {
            return false;
        }

        $minutesSince = (now()->timestamp - $lastValidAt) / 60;

        return $minutesSince <= (int) config('geolicense.grace_period_minutes', 30);
    }
}
