package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.*;
import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.dto.response.AuthRegisterDTO;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.security.jwt.JwtService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    public AuthRegisterDTO register(RegisterRequest request) {
        boolean userExist = userRepo.findByEmail(request.getEmail()).isPresent();

        if (userExist) {
            String message = messagesUtils.getMessage("userservice.user.exist", request.getEmail());
            logger.warning(message);
            throw new ExistingException(message);
        }

        Date now = new Date();
        User savedUser = new User();
        savedUser.setFullName(request.getFullName());
        savedUser.setEmail(request.getEmail());
        savedUser.setPassword(Objects.requireNonNull(passwordEncoder.encode(request.getPassword())));
        savedUser.setCreatedDate(now);
        savedUser.setModifiedDate(now);
        savedUser.setRole(Role.USER);
        savedUser.setCreatedBy("System");
        savedUser.setModifiedBy("System");

        User userResult = userRepo.save(savedUser);

        AuthRegisterDTO result = new AuthRegisterDTO();
        result.setUser(convertToUserDTO(userResult));
        result.setToken(jwtService.generateToken(userResult));

        logger.info("User registered successfully: " + userResult.getEmail());

        return result;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId().toString());
        assert user.getRole() != null;
        userDTO.setRole(user.getRole().toString());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }

    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}
