# GeoLicense — Laravel Client Setup

Compatible with **Laravel 11, 12, and 13**.

---

## 1. Copy Files

Copy the following files into your Laravel project, preserving the directory structure:

```
config/geolicense.php
app/Services/GeoLicense/MachineIdGenerator.php
app/Services/GeoLicense/LicenseHolder.php
app/Services/GeoLicense/LicenseActivationService.php
app/Services/GeoLicense/LicenseVerificationService.php
app/Http/Middleware/LicenseValidationMiddleware.php
app/Console/Commands/VerifyLicenseCommand.php
app/Providers/GeoLicenseServiceProvider.php
```

---

## 2. Set Environment Variables

Add to your `.env` file:

```env
GEOLICENSE_SERVER_URL=http://localhost:8082
GEOLICENSE_LICENSE_KEY=XXXX-XXXX-XXXX-XXXX
```

Optional overrides:

```env
GEOLICENSE_VERIFY_INTERVAL_MINS=60
GEOLICENSE_GRACE_PERIOD_MINS=30
```

---

## 3. Register the Service Provider

In `bootstrap/providers.php`:

```php
return [
    App\Providers\AppServiceProvider::class,
    App\Providers\GeoLicenseServiceProvider::class,  // add this
];
```

---

## 4. Register the Middleware

In `bootstrap/app.php`:

```php
->withMiddleware(function (Middleware $middleware) {
    $middleware->append(\App\Http\Middleware\LicenseValidationMiddleware::class);
})
```

This applies the license check globally to all routes.
To protect only specific routes instead, skip the global registration and apply it per-route:

```php
Route::middleware(\App\Http\Middleware\LicenseValidationMiddleware::class)
    ->group(function () {
        // your protected routes
    });
```

---

## 5. Schedule Periodic Verification

In `routes/console.php`:

```php
use Illuminate\Support\Facades\Schedule;

Schedule::command('geolicense:verify')->everyHour()->withoutOverlapping();
```

Then make sure your server cron runs the Laravel scheduler:

```cron
* * * * * cd /path-to-your-project && php artisan schedule:run >> /dev/null 2>&1
```

---

## 6. Verify It Works

```bash
# Manual activation test
php artisan geolicense:verify

# Check cache state (requires tinker)
php artisan tinker
>>> app(\App\Services\GeoLicense\LicenseHolder::class)->isValid()
```

---

## How It Works

| Step | What happens |
|---|---|
| App boots | `GeoLicenseServiceProvider::boot()` checks cache for a token |
| No token found | Calls `POST /api/v1/licenses/activate` → stores token in cache |
| Every request | `LicenseValidationMiddleware` checks `isValid()` from cache |
| Every hour | `geolicense:verify` artisan command calls `POST /api/v1/licenses/verify` |
| Server unreachable | Grace period allows traffic for N minutes after last successful check |
| License invalid | Returns HTTP 503 JSON response |

---

## Exclude Paths from License Check

Edit `config/geolicense.php`:

```php
'exclude_paths' => [
    'health',
    'up',
    'api/webhook/*',  // supports wildcards via Str::is()
],
```
