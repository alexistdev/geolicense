/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuTest {

    private Menu menu;
    private UUID id;
    private String name;
    private String urlink;
    private String classlink;
    private String icon;
    private int sortOrder;
    private int typeMenu;
    private UUID parentId;
    private String createdBy;
    private String modifiedBy;
    private java.util.Date createdDate;
    private java.util.Date modifiedDate;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        menu = new Menu();
        id = UUID.randomUUID();
        name = "Dashboard";
        urlink = "/dashboard";
        classlink = "DashboardController";
        icon = "home";
        sortOrder = 1;
        typeMenu = 1;
        parentId = UUID.randomUUID();
        createdBy = "System";
        modifiedBy = "System";
        createdDate = new java.util.Date();
        modifiedDate = new java.util.Date();

        menu.setId(id);
        menu.setName(name);
        menu.setUrlink(urlink);
        menu.setClasslink(classlink);
        menu.setIcon(icon);
        menu.setSortOrder(sortOrder);
        menu.setTypeMenu(typeMenu);
        menu.setParentId(parentId);
        menu.setCreatedBy(createdBy);
        menu.setModifiedBy(modifiedBy);
        menu.setCreatedDate(createdDate);
        menu.setModifiedDate(modifiedDate);
        menu.setDeleted(false);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test get data")
    void testGetData() {
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, menu.getId());
        Assertions.assertEquals(name, menu.getName());
        Assertions.assertEquals(urlink, menu.getUrlink());
        Assertions.assertEquals(classlink, menu.getClasslink());
        Assertions.assertEquals(icon, menu.getIcon());
        Assertions.assertEquals(sortOrder, menu.getSortOrder());
        Assertions.assertEquals(typeMenu, menu.getTypeMenu());
        Assertions.assertEquals(parentId, menu.getParentId());
        Assertions.assertEquals(createdBy, menu.getCreatedBy());
        Assertions.assertEquals(modifiedBy, menu.getModifiedBy());
        Assertions.assertEquals(createdDate, menu.getCreatedDate());
        Assertions.assertEquals(modifiedDate, menu.getModifiedDate());
        Assertions.assertFalse(menu.getDeleted());
        Assertions.assertNotNull(createdDate);
        Assertions.assertNotNull(modifiedDate);
    }

    @Test
    @Order(2)
    @DisplayName("2. Test set data")
    void testSetData() {
        String newName = "New Name";
        String newUrlink = "/new-urlink";
        String newClasslink = "NewClasslink";
        String newIcon = "new-icon";
        int newSortOrder = 2;
        int newTypeMenu = 2;
        UUID newParentId = UUID.randomUUID();
        String newCreatedBy = "New User";
        String newModifiedBy = "Updated User";
        Date newCreatedDate = new Date();
        Date newModifiedDate = new Date();

        menu.setName(newName);
        menu.setUrlink(newUrlink);
        menu.setClasslink(newClasslink);
        menu.setIcon(newIcon);
        menu.setSortOrder(newSortOrder);
        menu.setTypeMenu(newTypeMenu);
        menu.setParentId(newParentId);
        menu.setCreatedBy(newCreatedBy);
        menu.setModifiedBy(newModifiedBy);
        menu.setCreatedDate(newCreatedDate);
        menu.setModifiedDate(newModifiedDate);
        menu.setDeleted(true);

        Assertions.assertEquals(newName, menu.getName());
        Assertions.assertEquals(newUrlink, menu.getUrlink());
        Assertions.assertEquals(newClasslink, menu.getClasslink());
        Assertions.assertEquals(newIcon, menu.getIcon());
        Assertions.assertEquals(newSortOrder, menu.getSortOrder());
        Assertions.assertEquals(newTypeMenu, menu.getTypeMenu());
        Assertions.assertEquals(newParentId, menu.getParentId());
        Assertions.assertEquals(newCreatedBy, menu.getCreatedBy());
        Assertions.assertEquals(newModifiedBy, menu.getModifiedBy());
        Assertions.assertEquals(newCreatedDate, menu.getCreatedDate());
        Assertions.assertEquals(newModifiedDate, menu.getModifiedDate());
        Assertions.assertTrue(menu.getDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("3. Ensure Menu is Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(java.io.Serializable.class, menu);
    }

    @Test
    @Order(4)
    @DisplayName("4. Test extends BaseEntity")
    void testExtendsBaseEntity() {
        Assertions.assertInstanceOf(BaseEntity.class, menu);
    }

    @Test
    @Order(5)
    @DisplayName("5. Test id is UUID")
    void testIdIsUUID() {
        try {
            Field field = Menu.class.getDeclaredField("id");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("id should be UUID");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Test not blank validation on name")
    void testNameNotBlankValidation() {
        menu.setName("");

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank name");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be on the name field"
        );
    }

    @Test
    @Order(7)
    @DisplayName("7. Test name size validation")
    void testNameSizeValidation() {
        menu.setName("A".repeat(151));

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when name exceeds 150 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be on the name field"
        );
    }

    @Test
    @Order(8)
    @DisplayName("8. Test not blank validation on url link")
    void testUrlLinkNotBlankValidation() {
        menu.setUrlink("");

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank url link");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("urlink")),
                "Violation should be on the urlink field"
        );
    }

    @Test
    @Order(9)
    @DisplayName("9. Test url link size validation")
    void testUrlinkSizeValidation() {
        menu.setUrlink("A".repeat(151));

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when urlink exceeds 150 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("urlink")),
                "Violation should be on the urlink field"
        );
    }

    @Test
    @Order(10)
    @DisplayName("10. Test not blank validation on class link")
    void testClassLinkNotBlankValidation() {
        menu.setClasslink("");

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank class link");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("classlink")),
                "Violation should be on the class link field"
        );
    }

    @Test
    @Order(11)
    @DisplayName("11. Test class link size validation")
    void testClassLinkSizeValidation() {
        menu.setClasslink("A".repeat(151));

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when class link exceeds 150 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("classlink")),
                "Violation should be on the class link field"
        );
    }

    @Test
    @Order(12)
    @DisplayName("12. Test icon can be null")
    void testIconNullable() {
        menu.setIcon(null);

        var violations = validator.validate(menu);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("icon")),
                "Null icon should be allowed"
        );
    }

    @Test
    @Order(13)
    @DisplayName("13. Test icon size validation")
    void testIconSizeValidation() {
        menu.setIcon("A".repeat(51));

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when class link exceeds 51 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("icon")),
                "Violation should be on the class link field"
        );
    }


    @Test
    @Order(14)
    @DisplayName("14. Test default value for sortOrder")
    void testSortOrderDefaultValue() {
        Menu newMenu = new Menu();
        Assertions.assertEquals(0, newMenu.getSortOrder(), "sortOrder should have a default value of 0");
    }

    @Test
    @Order(15)
    @DisplayName("15. Test parentId can be null")
    void testParentIDNullable() {
        menu.setParentId(null);

        var violations = validator.validate(menu);
        Assertions.assertTrue(
                violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("parentId")),
                "Null parentId should be allowed"
        );
    }

    @Test
    @Order(16)
    @DisplayName("16. Test ParentID is UUID")
    void testParentIdIsUUID() {
        try {
            Field field = Menu.class.getDeclaredField("parentId");
            field.setAccessible(true);
            Assertions.assertEquals(UUID.class, field.getType());
        } catch (Exception e) {
            Assertions.fail("Parent ID should be UUID");
        }
    }

    @Test
    @Order(17)
    @DisplayName("17. Test default value for typeMenu")
    void testTypeMenuDefaultValue() {
        Menu newMenu = new Menu();
        Assertions.assertEquals(0, newMenu.getTypeMenu(), "typeMenu should have a default value of 0");
    }

    @Test
    @Order(18)
    @DisplayName("18. Test not blank validation on code")
    void testCodeNotBlankValidation() {
        menu.setCode("");

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail for blank code");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")),
                "Violation should be on the class link field"
        );
    }

    @Test
    @Order(19)
    @DisplayName("19. Test code size validation")
    void testCodeSizeValidation() {
        menu.setCode("A".repeat(4));

        var violations = validator.validate(menu);
        Assertions.assertFalse(violations.isEmpty(), "Validation should fail when class link exceeds 3 characters");
        Assertions.assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")),
                "Violation should be on the class link field"
        );
    }

    @Test
    @Order(20)
    @DisplayName("20. Test equals and hashCode based on id")
    void testEqualsAndHashCode() {
        String newName = "Dashboard";
        String newUrlink = "/dashboard";
        String newClasslink = "DashboardController";
        String newIcon = "home";
        int newSortOrder = 0;
        int newTypeMenu = 0;
        UUID newParentId = UUID.randomUUID();
        String newCreatedBy = "System";
        String newModifiedBy = "System";
        Date newCreatedDate = new Date();
        Date newModifiedDate = new Date();

        Menu menu2 = new Menu();
        menu2.setId(id);
        menu2.setName(newName);
        menu2.setUrlink(newUrlink);
        menu2.setClasslink(newClasslink);
        menu2.setIcon(newIcon);
        menu2.setSortOrder(newSortOrder);
        menu2.setTypeMenu(newTypeMenu);
        menu2.setParentId(newParentId);
        menu2.setCreatedBy(newCreatedBy);
        menu2.setModifiedBy(newModifiedBy);
        menu2.setCreatedDate(newCreatedDate);
        menu2.setModifiedDate(newModifiedDate);

        Assertions.assertEquals(menu, menu2, "Menu with the same id should be equal");
        Assertions.assertEquals(menu.hashCode(), menu2.hashCode(),
                "LicenseTypes with the same id should have the same hashCode");
    }

    @Test
    @Order(21)
    @DisplayName("21. Test two Menus with different ids are not equal")
    void testNotEqualWithDifferentIds() {
        String newName = "Dashboard";
        String newUrlink = "/dashboard";
        String newClasslink = "DashboardController";
        String newIcon = "home";
        int newSortOrder = 0;
        int newTypeMenu = 0;
        UUID newParentId = UUID.randomUUID();
        String newCreatedBy = "System";
        String newModifiedBy = "System";
        Date newCreatedDate = new Date();
        Date newModifiedDate = new Date();


        Menu menu2 = new Menu();
        menu2.setId(UUID.randomUUID());
        menu2.setName(newName);
        menu2.setUrlink(newUrlink);
        menu2.setClasslink(newClasslink);
        menu2.setIcon(newIcon);
        menu2.setSortOrder(newSortOrder);
        menu2.setTypeMenu(newTypeMenu);
        menu2.setParentId(newParentId);
        menu2.setCreatedBy(newCreatedBy);
        menu2.setModifiedBy(newModifiedBy);
        menu2.setCreatedDate(newCreatedDate);
        menu2.setModifiedDate(newModifiedDate);

        Assertions.assertNotEquals(menu, menu2,
                "Menus with different ids should not be equal");
    }

    @Test
    @Order(22)
    @DisplayName("22. Test isDeleted defaults to false")
    void testIsDeletedDefaultsFalse() {
        Menu newMenu = new Menu();
        Assertions.assertFalse(newMenu.getDeleted(), "isDeleted should default to false");
    }

}
