package com.alexistdev.geolicense.dto.response;

import lombok.Getter;
import lombok.Setter;

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
    private String role;
    private List<MenuResponse> menus;
}
