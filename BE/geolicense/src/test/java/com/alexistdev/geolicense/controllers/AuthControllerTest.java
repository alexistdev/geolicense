package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.request.LoginRequest;
import com.alexistdev.geolicense.dto.request.RegisterRequest;
import com.alexistdev.geolicense.dto.UserDTO;
import com.alexistdev.geolicense.dto.response.AuthLoginResponse;
import com.alexistdev.geolicense.dto.response.AuthRegisterDTO;
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.security.jwt.JwtAuthenticationFilter;
import com.alexistdev.geolicense.security.jwt.JwtService;
import com.alexistdev.geolicense.services.AuthService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private MessagesUtils messagesUtils;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private RegisterRequest validRequest;
    private AuthRegisterDTO authRegisterDTO;

    private LoginRequest validLoginRequest;
    private AuthLoginResponse authLoginResponse;


    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .id("some-uuid")
                .fullName("John Doe")
                .email("john@example.com")
                .role("USER")
                .build();

        authRegisterDTO = AuthRegisterDTO.builder()
                .user(userDTO)
                .token("jwt-token")
                .build();

        validLoginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("securePassword123")
                .build();

        authLoginResponse = AuthLoginResponse.builder()
                .id("some-uuid")
                .token("login-jwt-token")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Register - success returns 201 CREATED")
    @WithMockUser
    void register_shouldReturn201OnSuccess() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print()).andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    @DisplayName("2. Register - success response has status true")
    @WithMockUser
    void register_shouldHaveStatusTrueOnSuccess() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @Order(3)
    @DisplayName("3. Register - success response contains success message")
    @WithMockUser
    void register_shouldContainSuccessMessage() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.messages[0]").value("Registration successful"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Register - success response payload contains JWT token")
    @WithMockUser
    void register_shouldContainJwtTokenInPayload() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.payload.token").value("jwt-token"));
    }

    @Test
    @Order(5)
    @DisplayName("5. Register - success response payload contains user email")
    @WithMockUser
    void register_shouldContainUserEmailInPayload() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.payload.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.payload.user.fullName").value("John Doe"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Register - calls authService.register once")
    @WithMockUser
    void register_shouldCallAuthServiceOnce() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print()).andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. Register - calls messagesUtils for success key")
    @WithMockUser
    void register_shouldCallMessagesUtilsWithSuccessKey() throws Exception {
        when(messagesUtils.getMessage("authcontroller.register.success")).thenReturn("Registration successful");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authRegisterDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print()).andExpect(status().isCreated());

        verify(messagesUtils, times(1)).getMessage(eq("authcontroller.register.success"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Register - duplicate user returns 409 CONFLICT")
    @WithMockUser
    void register_shouldReturn409WhenUserAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ExistingException("User john@example.com already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    @DisplayName("9. Register - duplicate user response has status false")
    @WithMockUser
    void register_shouldHaveStatusFalseOnConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ExistingException("User john@example.com already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @Order(10)
    @DisplayName("10. Register - duplicate user response contains error message")
    @WithMockUser
    void register_shouldContainErrorMessageOnConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ExistingException("User john@example.com already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(jsonPath("$.messages[0]").value("User john@example.com already exists"));
    }

    @Test
    @Order(11)
    @DisplayName("11. Register - duplicate user does not call messagesUtils for success key")
    @WithMockUser
    void register_shouldNotCallMessagesUtilsOnConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ExistingException("User john@example.com already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());

        verify(messagesUtils, never()).getMessage(eq("authcontroller.register.success"));
    }

    @Test
    @Order(12)
    @DisplayName("12. Login - success returns 200 OK")
    @WithMockUser
    void login_shouldReturn200OnSuccess() throws Exception{
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    @Order(13)
    @DisplayName("13. Login - success response has status true")
    @WithMockUser
    void login_shouldHaveStatusTrueOnSuccess() throws Exception{
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @Order(14)
    @DisplayName("14. Login - success response contains success message")
    @WithMockUser
    void login_shouldContainSuccessMessage() throws Exception {
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(jsonPath("$.messages[0]").value("Login successful"));
    }

    @Test
    @Order(15)
    @DisplayName("15. Login - success response payload contains JWT token")
    @WithMockUser
    void login_shouldContainJwtTokenInPayload() throws Exception {
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(jsonPath("$.payload.token").value("login-jwt-token"));
    }

    @Test
    @Order(16)
    @DisplayName("16. Login - calls authService.authenticate once")
    @WithMockUser
    void login_shouldCallAuthServiceOnce() throws Exception {
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andDo(print()).andExpect(status().isOk());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @Order(17)
    @DisplayName("17. Login - calls messagesUtils for login success key")
    @WithMockUser
    void login_shouldCallMessagesUtilsWithLoginSuccessKey() throws Exception {
        when(messagesUtils.getMessage("authcontroller.login.success")).thenReturn("Login successful");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andDo(print()).andExpect(status().isOk());

        verify(messagesUtils, times(1)).getMessage(eq("authcontroller.login.success"));
    }
}
