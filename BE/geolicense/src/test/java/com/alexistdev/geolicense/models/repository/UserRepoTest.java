package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class UserRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepo userRepo;

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setPassword("123456");
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private User persistAndClear(User user) {
        User persisted = entityManager.persistFlushFind(user);
        entityManager.clear();
        return persisted;
    }

    @Test
    @Order(1)
    @DisplayName("1. FindByEmail returns user when email exists")
    void findByEmailReturnsUserWhenEmailExists() {
        User saved = persistAndClear(createUser("Alexsander Hendra", "alexistdev@gmail.com", Role.USER));

        Optional<User> result = userRepo.findByEmail("alexistdev@gmail.com");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(saved.getId(), result.get().getId());
        Assertions.assertEquals("alexistdev@gmail.com", result.get().getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("2. FindByEmail returns empty when user is soft deleted")
    void findByEmailReturnsEmptyWhenUserSoftDeleted() {
        User deletedUser = createUser("Deleted User", "deleted@gmail.com", Role.USER);
        deletedUser.setDeleted(true);
        persistAndClear(deletedUser);

        Optional<User> result = userRepo.findByEmail("deleted@gmail.com");

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("3. FindByRoleNot excludes admin users")
    void findByRoleNotExcludesAdminUsers() {
        persistAndClear(createUser("user test1", "user1@gmail.com", Role.USER));
        persistAndClear(createUser("admin test", "admin@gmail.com", Role.ADMIN));

        Pageable pageable = Pageable.ofSize(10);
        Page<User> result = userRepo.findByRoleNot(Role.ADMIN, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertTrue(result.stream().allMatch(user -> user.getRole() == Role.USER));
        Assertions.assertFalse(result.stream().anyMatch(user -> user.getRole() == Role.ADMIN));
    }

    @Test
    @Order(4)
    @DisplayName("4. FindByFilter matches fullName or email case-insensitively and excludes admin")
    void findByFilterMatchesKeywordAndExcludesAdmin() {
        persistAndClear(createUser("Alex", "alex@gmail.com", Role.USER));
        persistAndClear(createUser("Hendra", "Hendra@mail.com", Role.USER));
        persistAndClear(createUser("Admin", "admin@gmail.com", Role.ADMIN));

        Page<User> byName = userRepo.findByFilter("Alex", PageRequest.of(0, 10));
        Page<User> byEmail = userRepo.findByFilter("Hendra", PageRequest.of(0, 10));

        Assertions.assertEquals(1, byName.getTotalElements());
        Assertions.assertEquals("alex@gmail.com", byName.getContent().getFirst().getEmail());
        Assertions.assertEquals(1, byEmail.getTotalElements());
        Assertions.assertEquals("Hendra@mail.com", byEmail.getContent().getFirst().getEmail());
        Assertions.assertFalse(byName.stream().anyMatch(user -> user.getRole() == Role.ADMIN));
    }

    @Test
    @Order(5)
    @DisplayName("5. FindByFilter supports pagination")
    void findByFilterSupportsPagination() {
        persistAndClear(createUser("Keyword One", "keyword1@mail.com", Role.USER));
        persistAndClear(createUser("Keyword Two", "keyword2@mail.com", Role.USER));
        persistAndClear(createUser("Keyword Three", "keyword3@mail.com", Role.USER));

        Page<User> firstPage = userRepo.findByFilter("keyword", PageRequest.of(0, 2));
        Page<User> secondPage = userRepo.findByFilter("keyword", PageRequest.of(1, 2));

        Assertions.assertEquals(3, firstPage.getTotalElements());
        Assertions.assertEquals(2, firstPage.getNumberOfElements());
        Assertions.assertEquals(1, secondPage.getNumberOfElements());
    }
}
