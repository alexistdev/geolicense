package com.alexistdev.geolicense.dto.response;

import com.alexistdev.geolicense.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegisterDTO {

    private UserDTO user;
    private String token;

}
