package com.alexistdev.geolicense.models.services;

import com.alexistdev.geolicense.dto.AuthResponseDTO;
import com.alexistdev.geolicense.dto.RegisterRequestDTO;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private AuthenticationManager authenticationManager;
    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequestDTO.builder()
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
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    @Order(2)
    @DisplayName("2. Test register method saves user with correct fields")
    void register_shouldSaveUserWithCorrectFields() {
        User savedUser = new User();
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getFullName()).isEqualTo("John Doe");
        assertThat(capturedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);
        assertThat(capturedUser.getCreatedBy()).isEqualTo("System");
        assertThat(capturedUser.getModifiedBy()).isEqualTo("System");
        assertThat(capturedUser.getCreatedDate()).isNotNull();
        assertThat(capturedUser.getModifiedDate()).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("3. Test register method throws ExistingException when user already exists")
    void register_shouldThrowExistingExceptionWhenUserAlreadyExists() {
        User existingUser = new User();
        existingUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(messagesUtils.getMessage(eq("user.already.exist"), eq(registerRequest.getEmail())))
                .thenReturn("User john@example.com already exists");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ExistingException.class)
                .hasMessage("User john@example.com already exists");

        verify(userRepo, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Test register method encodes password before saving")
    void register_shouldEncodePasswordBeforeSaving() {
        User savedUser = new User();
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$encodedHash");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("securePassword123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$encodedHash");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test register method generates token for saved user")
    void register_shouldGenerateTokenForSavedUser() {
        User savedUser = new User();
        savedUser.setEmail(registerRequest.getEmail());

        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("generated-jwt");

        AuthResponseDTO response = authService.register(registerRequest);

        verify(jwtService).generateToken(savedUser);
        assertThat(response.getToken()).isEqualTo("generated-jwt");
    }
}
