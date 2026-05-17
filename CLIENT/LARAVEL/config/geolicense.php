<?php

return [

    /*
     * Base URL of the GeoLicense server.
     * Example: http://localhost:8082  or  https://license.yourdomain.com
     */
    'server_url' => env('GEOLICENSE_SERVER_URL', 'http://localhost:8082'),

    /*
     * License key issued by the GeoLicense server for this installation.
     */
    'license_key' => env('GEOLICENSE_LICENSE_KEY'),

    /*
     * How often (in minutes) the background scheduler re-verifies the license.
     * Matches the artisan schedule defined in routes/console.php.
     */
    'verify_interval_mins' => env('GEOLICENSE_VERIFY_INTERVAL_MINS', 60),

    /*
     * If the license server is unreachable, allow requests for this many minutes
     * after the last successful verification before blocking traffic.
     */
    'grace_period_minutes' => env('GEOLICENSE_GRACE_PERIOD_MINS', 30),

    /*
     * URI patterns that bypass the license check entirely.
     * Supports wildcard (*) matching via Str::is().
     */
    'exclude_paths' => [
        'health',
        'up',
    ],

];
