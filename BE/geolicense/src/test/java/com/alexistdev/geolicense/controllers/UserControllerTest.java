package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.security.jwt.JwtService;
import com.alexistdev.geolicense.services.UserService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MessagesUtils messagesUtils;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    private static final String SESSION_ID = "f89ae0ac-a92a-4e28-915a-fad87f893f44";
    private static final String USER_ID = "7956a330-3f3f-4396-9afc-16b039f86948";
    private static final String FAKE_JWT = "eyJhbGciOiJIUzI1NiJ9.fake.token";

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.fromString(USER_ID));
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole(Role.ADMIN);
        adminUser.setCreatedDate(new Date());
        adminUser.setModifiedDate(new Date());
        adminUser.setCreatedBy("System");
        adminUser.setModifiedBy("System");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private void setupValidSession() {

        when(valueOperations.get(SESSION_ID)).thenReturn(FAKE_JWT);

        when(jwtService.extractUsername(FAKE_JWT)).thenReturn("admin@example.com");
        when(jwtService.isTokenValid(eq(FAKE_JWT), any())).thenReturn(true);

        when(userDetailsService.loadUserByUsername("admin@example.com")).thenReturn(adminUser);
    }

    private void setupUserServiceMock() {
        User sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setFullName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setRole(Role.USER);
        sampleUser.setCreatedDate(new Date());
        sampleUser.setModifiedDate(new Date());
        sampleUser.setCreatedBy("System");
        sampleUser.setModifiedBy("System");

        Page<User> usersPage = new PageImpl<>(List.of(sampleUser));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(usersPage);
        when(messagesUtils.getMessage("usercontroller.user.nouser")).thenReturn("No users found");
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /users with valid SID cookie - returns 200 OK")
    void getAllUsers_withValidSidCookie_shouldReturn200() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /users with valid SID cookie - response has status true")
    void getAllUsers_withValidSidCookie_shouldHaveStatusTrue() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /users with valid SID cookie - response contains user data")
    void getAllUsers_withValidSidCookie_shouldContainUserData() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andExpect(jsonPath("$.payload.content").isArray())
                .andExpect(jsonPath("$.payload.content[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.payload.content[0].email").value("john@example.com"));
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /users with valid SID cookie - calls userService once")
    void getAllUsers_withValidSidCookie_shouldCallUserServiceOnce() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /users with valid SID cookie - Redis is queried with session ID")
    void getAllUsers_withValidSidCookie_shouldQueryRedis() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andExpect(status().isOk());

        verify(valueOperations, times(1)).get(SESSION_ID);
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /users without cookie or header - returns 403 Forbidden")
    void getAllUsers_withoutAuthentication_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /users with invalid SID cookie (not in Redis) - returns 403 Forbidden")
    void getAllUsers_withInvalidSidCookie_shouldReturn403() throws Exception {
        when(valueOperations.get("invalid-session-id")).thenReturn(null);

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", "invalid-session-id")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /users with valid SID cookie - empty page returns status false")
    void getAllUsers_withValidSidCookie_emptyPage_shouldHaveStatusFalse() throws Exception {
        setupValidSession();

        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);
        when(messagesUtils.getMessage("usercontroller.user.nouser")).thenReturn("No users found");

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID)))
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.messages[0]").value("No users found"));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /users with valid SID cookie - supports pagination params")
    void getAllUsers_withValidSidCookie_supportsPagination() throws Exception {
        setupValidSession();
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new Cookie("SID", SESSION_ID))
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "email")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @Order(10)
    @DisplayName("10. GET /users with Bearer token still works (backward compatible)")
    void getAllUsers_withBearerToken_shouldReturn200() throws Exception {
        when(jwtService.extractUsername(FAKE_JWT)).thenReturn("admin@example.com");
        when(jwtService.isTokenValid(eq(FAKE_JWT), any())).thenReturn(true);
        when(userDetailsService.loadUserByUsername("admin@example.com")).thenReturn(adminUser);
        setupUserServiceMock();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + FAKE_JWT))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
