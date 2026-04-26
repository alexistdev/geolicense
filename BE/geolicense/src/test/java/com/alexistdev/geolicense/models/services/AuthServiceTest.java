/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.services;

import com.alexistdev.geolicense.dto.UserDTO;
import com.alexistdev.geolicense.dto.request.LoginRequest;
import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.dto.response.AuthLoginResponse;
import com.alexistdev.geolicense.dto.response.AuthRegisterDTO;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.security.jwt.JwtService;
import com.alexistdev.geolicense.services.AuthService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private MessagesUtils messagesUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private StringRedisTemplate mockRedisTemplate;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("securePassword123")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Test register method")
    void register_shouldReturnTokenWhenUserDoesNotExist() {
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setRole(Role.USER);
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthRegisterDTO response = authService.register(registerRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("jwt-token", response.getToken());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test register method saves user with correct fields")
    void register_shouldSaveUserWithCorrectFields() {
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setRole(Role.USER);
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        Assertions.assertEquals("John Doe", capturedUser.getFullName());
        Assertions.assertEquals("john@example.com", capturedUser.getEmail());
        Assertions.assertEquals("encodedPassword", capturedUser.getPassword());
        Assertions.assertEquals(Role.USER, capturedUser.getRole());
        Assertions.assertEquals("System", capturedUser.getCreatedBy());
        Assertions.assertEquals("System", capturedUser.getModifiedBy());
        Assertions.assertNotNull(capturedUser.getCreatedDate());
        Assertions.assertNotNull(capturedUser.getModifiedDate());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test register method throws ExistingException when user already exists")
    void register_shouldThrowExistingExceptionWhenUserAlreadyExists() {
        User existingUser = new User();
        existingUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(messagesUtils.getMessage(eq("userservice.user.exist"), eq(registerRequest.getEmail())))
                .thenReturn("User john@example.com already exists");

        Assertions.assertThrows(ExistingException.class, () -> authService.register(registerRequest));

        verify(userRepo, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test register method encodes password before saving")
    void register_shouldEncodePasswordBeforeSaving() {
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setRole(Role.USER);
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$encodedHash");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("securePassword123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        Assertions.assertEquals("$2a$10$encodedHash", userCaptor.getValue().getPassword());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test register method generates token for saved user")
    void register_shouldGenerateTokenForSavedUser() {
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setRole(Role.USER);
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("generated-jwt");

        AuthRegisterDTO response = authService.register(registerRequest);

        verify(jwtService).generateToken(savedUser);
        Assertions.assertEquals("generated-jwt", response.getToken());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test authenticate method returns token for valid credentials")
    void authenticate_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alexistdev@gmail.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Alexsander Hendra Wijaya");
        user.setEmail(loginRequest.getEmail());
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        org.springframework.security.core.Authentication mockAuthentication =
                mock(org.springframework.security.core.Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
        when(jwtService.generateToken(user)).thenReturn("valid-jwt-token");

        AuthLoginResponse response = authService.authenticate(loginRequest);
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        verify(jwtService).generateToken(user);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("valid-jwt-token", response.getToken());
        Assertions.assertEquals(user.getId().toString(), response.getId());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test authenticate method throws exception for invalid credentials")
    void authenticate_shouldThrowException_whenCredentialsAreInvalid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        Assertions.assertThrows(BadCredentialsException.class, () ->
            authService.authenticate(loginRequest));

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @Order(8) // Adjust order as needed
    @DisplayName("8. Test convertToUserDTO method maps fields correctly")
    void convertToUserDTO_shouldMapUserFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFullName("Jane Doe");
        user.setEmail("jane.doe@example.com");
        user.setRole(Role.ADMIN);

        UserDTO userDTO = authService.convertToUserDTO(user);

        Assertions.assertNotNull(userDTO);
        Assertions.assertEquals(userId.toString(), userDTO.getId());
        Assertions.assertEquals("Jane Doe", userDTO.getFullName());
        Assertions.assertEquals("jane.doe@example.com", userDTO.getEmail());
        Assertions.assertEquals(Role.ADMIN.toString(), userDTO.getRole());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test convertToUserDTO throws error for null role")
    void convertToUserDTO_shouldThrowErrorForNullRole() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFullName("Jane Doe");
        user.setEmail("jane.doe@example.com");
        user.setRole(null);

        Assertions.assertThrows(AssertionError.class, () ->
            authService.convertToUserDTO(user));
    }

    @Test
    @Order(10)
    @DisplayName("10. Test authenticate stores JWT in Redis")
    void authenticate_shouldStoreJwtInRedisWhenPresent() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail(loginRequest.getEmail());
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        org.springframework.security.core.Authentication mockAuthentication =
                mock(org.springframework.security.core.Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);

        String generatedJwt = "redis-jwt-token";
        when(jwtService.generateToken(user)).thenReturn(generatedJwt);

        ValueOperations<String, String> mockValueOperations = mock(ValueOperations.class);
        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOperations);

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jwtTokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        doNothing().when(mockValueOperations).set(anyString(), anyString(), any(Duration.class));

        AuthLoginResponse response = authService.authenticate(loginRequest);

        verify(mockValueOperations).set(sessionIdCaptor.capture(), jwtTokenCaptor.capture(), durationCaptor.capture());

        Assertions.assertNotNull(sessionIdCaptor.getValue(), "Session ID should have been generated");
        Assertions.assertEquals(generatedJwt, jwtTokenCaptor.getValue());
        Assertions.assertEquals(Duration.ofHours(1), durationCaptor.getValue());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(generatedJwt, response.getToken());
        Assertions.assertEquals(user.getId().toString(), response.getId());
    }
}
