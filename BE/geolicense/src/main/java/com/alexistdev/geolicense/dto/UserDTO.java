package com.alexistdev.geolicense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private boolean isSuspended;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
