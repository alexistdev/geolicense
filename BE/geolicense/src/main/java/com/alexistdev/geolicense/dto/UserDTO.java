package com.alexistdev.geolicense.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private boolean isSuspended;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
