/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMenuTest {
    private RoleMenu roleMenu;
    private Menu menu;
    private Validator validator;

    @BeforeEach
    void setUp() {
        roleMenu = new RoleMenu();
        menu = new Menu();
        menu.setName("Dashboard");
        menu.setUrlink("/dashboard");
        menu.setSortOrder(1);
        menu.setIsDeleted(false);
        menu.setCreatedBy("System");
        menu.setModifiedDate(new java.util.Date());
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedBy("System");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    @Test
    @Order(1)
    @DisplayName("1. Test Getter And Setter")
    void testGetterAndSetter() {
        roleMenu.setRole(Role.USER);
        roleMenu.setMenu(menu);
        Assertions.assertEquals(Role.USER,roleMenu.getRole());
        Assertions.assertEquals(menu,roleMenu.getMenu());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Inherited Audit Fields")
    void testInheritedAuditFields() {
        String createdBy = "System";
        String modifiedBy = "System";
        Date dateCreatedModified = new java.util.Date();
        roleMenu.setCreatedBy(createdBy);
        roleMenu.setModifiedBy(modifiedBy);
        roleMenu.setCreatedDate(dateCreatedModified);
        roleMenu.setModifiedDate(dateCreatedModified);
        Assertions.assertEquals(createdBy,roleMenu.getCreatedBy());
        Assertions.assertEquals(modifiedBy,roleMenu.getModifiedBy());
        Assertions.assertEquals(dateCreatedModified,roleMenu.getCreatedDate());
        Assertions.assertEquals(dateCreatedModified,roleMenu.getModifiedDate());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test IsDeleted Default Value")
    void testIsDeletedDefaultValue() {
        Assertions.assertFalse(roleMenu.getIsDeleted());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test IsDeleted Setter")
    void testIsDeletedSetter() {
        roleMenu.setIsDeleted(true);
        Assertions.assertTrue(roleMenu.getIsDeleted());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,roleMenu);
    }

    @Test
    @Order(6)
    @DisplayName("6. Test RoleMenu Entity")
    void testRoleMenuEntity() {
        Assertions.assertInstanceOf(RoleMenu.class,new RoleMenu());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Valid RoleMenu")
    void testValidRoleMenu() {
        roleMenu.setRole(Role.USER);
        roleMenu.setMenu(menu);
        roleMenu.setIsDeleted(false);
        roleMenu.setCreatedBy("System");
        roleMenu.setCreatedDate(new Date());
        roleMenu.setModifiedBy("System");
        roleMenu.setModifiedDate(new Date());

        Assertions.assertEquals(Role.USER,roleMenu.getRole());
        Assertions.assertEquals(menu,roleMenu.getMenu());
        Assertions.assertFalse(roleMenu.getIsDeleted());
        Assertions.assertNotNull(roleMenu.getCreatedDate());
        Assertions.assertNotNull(roleMenu.getModifiedDate());
        Assertions.assertNotNull(roleMenu.getCreatedBy());
        Assertions.assertNotNull(roleMenu.getModifiedBy());
    }

    @Test
    @Order(8)
    @DisplayName("8. Test Admin Enum")
    void testAdminEnum() {
        roleMenu.setRole(Role.ADMIN);
        Assertions.assertEquals(Role.ADMIN,roleMenu.getRole());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test User Enum")
    void testUserEnum() {
        roleMenu.setRole(Role.USER);
        Assertions.assertEquals(Role.USER,roleMenu.getRole());
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Role is Required")
    void testRoleIsRequired() {
        roleMenu.setMenu(menu);
        roleMenu.setRole(null);

        Set<ConstraintViolation<RoleMenu>> violations = validator.validate(roleMenu);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("11. Test Menu is Required")
    void testMenuIsRequired() {
        roleMenu.setMenu(null);
        roleMenu.setRole(Role.USER);

        Set<ConstraintViolation<RoleMenu>> violations = validator.validate(roleMenu);
        Assertions.assertFalse(violations.isEmpty());
    }
}
