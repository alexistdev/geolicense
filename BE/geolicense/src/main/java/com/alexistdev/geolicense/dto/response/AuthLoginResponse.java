package com.alexistdev.geolicense.dto.response;

import java.util.List;

public record AuthLoginResponse(
        String id,
        String sessionToken,
        String fullName,
        String role,
        List<MenuResponse> menus,
        String homeURL
) {}
