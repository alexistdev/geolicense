package com.alexistdev.geolicense.models.entity;

import org.junit.jupiter.api.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    @Test
    @Order(1)
    @DisplayName("1. Test when user has role then getAuthorities returns correct authorities")
    void whenUserHasRoleThenGetAuthoritiesReturnsCorrectAuthorities() {
        Role role = Role.ADMIN;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(role);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(1, authorities.size(), "User should have one authority");
        assertEquals(new SimpleGrantedAuthority(role.name()), authorities.iterator().next(),
                "Authority should match the user's role");
    }

    @Test
    @Order(2)
    @DisplayName("2. Test when user has no role then getAuthorities returns empty authorities")
    void whenUserRoleIsNullThenGetAuthoritiesReturnsEmptyAuthorities() {

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(null);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(Collections.emptyList(), authorities, "User with no role should have no authorities");
    }

    @Test
    @Order(3)
    @DisplayName("3. Test when user has different role then getAuthorities returns correct authority")
    void whenUserHasDifferentRoleThenGetAuthoritiesReturnsCorrectAuthority() {

        Role role = Role.USER;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("securepassword");
        user.setRole(role);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities, "Authorities should not be null");
        assertEquals(1, authorities.size(), "User should have one authority");
        assertEquals(new SimpleGrantedAuthority(role.name()), authorities.iterator().next(),
                "Authority should match the user's role");
    }

    @Test
    @Order(4)
    @DisplayName("4. Test isAccountNonExpired returns true")
    void isAccountNonExpiredReturnsTrue() {
        User user = new User();
        Assertions.assertTrue(user.isAccountNonExpired(), "Account should not be expired");
    }

    @Test
    @Order(5)
    @DisplayName("5. Test isAccountNonLocked returns true when user is not suspended")
    void isAccountNonLockedReturnsTrueWhenUserIsNotSuspended() {
        User user = new User();
        user.setSuspended(false);
        Assertions.assertTrue(user.isAccountNonLocked(), "Account should not be locked");
    }

    @Test
    @Order(6)
    @DisplayName("6. Test isAccountNonLocked returns false when user is suspended")
    void isAccountNonLockedReturnsFalseWhenUserIsSuspended() {
        User user = new User();
        user.setSuspended(true);
        Assertions.assertFalse(user.isAccountNonLocked(), "Account should be locked");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test isCredentialsNonExpired returns true")
    void isCredentialsNonExpiredReturnsTrue() {
        User user = new User();
        Assertions.assertTrue(user.isCredentialsNonExpired(), "Credentials should not be expired");
    }

    @Test
    @Order(8)
    @DisplayName("8. Test isEnabled returns true when user is not suspended")
    void isEnabledReturnsTrueWhenUserIsNotSuspended() {
        User user = new User();
        user.setSuspended(false);
        Assertions.assertTrue(user.isEnabled(), "User should be enabled");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test isEnabled returns false when user is suspended")
    void isEnabledReturnsFalseWhenUserIsSuspended() {
        User user = new User();
        user.setSuspended(true);
        Assertions.assertFalse(user.isEnabled(), "User should be disabled");
    }

    @Test
    @Order(10)
    @DisplayName("10. Test getUsername returns the email")
    void getUsernameReturnsTheEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        Assertions.assertEquals(email, user.getUsername(), "Username should be the user's email");
    }

    @Test
    @Order(11)
    @DisplayName("11. Test isSuspended returns true when user is suspended")
    void isSuspendedReturnsTrueWhenUserIsSuspended() {
        User user = new User();
        user.setSuspended(true);
        Assertions.assertTrue(user.isSuspended(), "User should be suspended");
    }

    @Test
    @Order(12)
    @DisplayName("12. Test isSuspended returns false when user is not suspended")
    void isSuspendedReturnsFalseWhenUserIsNotSuspended() {
        User user = new User();
        user.setSuspended(false);
        Assertions.assertFalse(user.isSuspended(), "User should not be suspended");
    }

}
