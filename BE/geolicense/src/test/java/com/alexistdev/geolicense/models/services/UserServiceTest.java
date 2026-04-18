package com.alexistdev.geolicense.models.services;

import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.services.UserService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        Assertions.assertEquals(users.size(), result.getContent().size());
        Assertions.assertEquals(user1.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(user2.getId(), result.getContent().get(1).getId());
    }
}
