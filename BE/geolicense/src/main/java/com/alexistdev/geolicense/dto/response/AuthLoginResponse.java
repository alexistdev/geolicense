package com.alexistdev.geolicense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginResponse {
    private String id;
    private String sessionToken;
    private String fullName;
    private String role;
    private List<MenuResponse> menus;
    private String homeURL;
}
