package com.alexistdev.geolicense.models.services;

import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.services.UserService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setRole(Role.USER);
        testUser.setSuspended(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get all users")
    void testGetAllUsers() {
        Pageable pageable = Pageable.unpaged();
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(new UUID(1L, 1L));
        user1.setRole(Role.USER);
        users.add(user1);
        User user2 = new User();
        user2.setId(new UUID(2L, 2L));
        user2.setRole(Role.USER);
        users.add(user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepo.findByRoleNot(Role.ADMIN, pageable)).thenReturn(userPage);

        Page<User> result = userService.getAllUsers(pageable);

        assertEquals(users.size(), result.getContent().size());
        assertEquals(user1.getId(), result.getContent().get(0).getId());
        assertEquals(user2.getId(), result.getContent().get(1).getId());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test find user by id - user found")
    void testFindUserById_found() {
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.findUserById(testUserId.toString());

        assertEquals(testUserId.toString(), result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(Role.USER.toString(), result.getRole());
        assertFalse(result.isSuspended());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test find user by id - user not found")
    void testFindUserById_notFound() {
        String notFoundId = testUserId.toString();
        String expectedMessage = "User with id " + notFoundId + " not found";
        when(userRepo.findById(testUserId)).thenReturn(Optional.empty());
        when(messagesUtils.getMessage("userservice.user.notfound", notFoundId))
                .thenReturn(expectedMessage);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findUserById(notFoundId));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test find user by id - invalid UUID format")
    void testFindUserById_invalidUUID() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserById("not-a-valid-uuid"));
    }
}
