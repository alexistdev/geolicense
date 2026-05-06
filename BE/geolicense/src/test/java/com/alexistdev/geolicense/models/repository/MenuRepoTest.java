/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Menu;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class MenuRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MenuRepo menuRepo;

    @BeforeEach
    void setUp() {
        Menu menu1 = createMenu("dashboard", "/dashboard",
                "dashboardClass", "fa fa-icon", "DSH");
        menu1.setSortOrder(1);
        entityManager.persist(menu1);

        Menu menu2 = createMenu("master", "/master",
                "masterClass", "fa fa-icon", "MST");
        menu2.setSortOrder(2);
        entityManager.persist(menu2);
        entityManager.flush();
    }

    private Menu createMenu(
            String name,
            String urlLink,
            String classLink,
            String setIcon,
            String code
    ){
        UUID parentID = UUID.randomUUID();

        Menu menu = new Menu();
        menu.setName(name);
        menu.setUrlink(urlLink);
        menu.setClasslink(classLink);
        menu.setSortOrder(1);
        menu.setIcon(setIcon);
        menu.setTypeMenu(1);
        menu.setCode(code);
        menu.setParentId(parentID);

        return menu;
    }

    @Test
    @Order(1)
    @DisplayName("1. Should save a new menu successfully")
    void testSaveMenu() {
        Menu settingsMenu = createMenu("settings", "/settings", "settingsClass", "fa fa-cogs", "SET");
        Menu savedMenu = menuRepo.save(settingsMenu);

        Assertions.assertNotNull(savedMenu);
        Assertions.assertEquals("settings",savedMenu.getName());

        Menu foundMenu = entityManager.find(Menu.class, savedMenu.getId());
        Assertions.assertNotNull(foundMenu);
        Assertions.assertEquals("settings", foundMenu.getName());
    }

    @Test
    @Order(2)
    @DisplayName("2. Should find menus that are not deleted")
    void testFindByIsDeletedFalse() {
        Menu deletedMenu = createMenu("deleted", "/deleted", "deletedClass", "fa fa-trash", "DEL");
        deletedMenu.setIsDeleted(true);
        entityManager.persist(deletedMenu);
        entityManager.flush();

        Page<Menu> resultPage = menuRepo.findByIsDeletedFalse(Pageable.unpaged());
        List<String> menuNames = resultPage.getContent().stream().map(Menu::getName).toList();
        Assertions.assertEquals(2, resultPage.getContent().size());
        Assertions.assertTrue(menuNames.containsAll(List.of("dashboard", "master")));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should find menu by name including deleted ones")
    void testFindByNameIncludingDeleted_FindsDeleted() {
        Menu deletedMenu = createMenu("deletedMenu", "/deleted", "deletedClass", "fa fa-trash", "DM1");
        deletedMenu.setIsDeleted(true);
        entityManager.persistAndFlush(deletedMenu);

        Optional<Menu> foundMenu = menuRepo.findByNameIncludingDeleted("deletedMenu");

        Assertions.assertTrue(foundMenu.isPresent());
        Assertions.assertEquals("deletedMenu", foundMenu.get().getName());
        Assertions.assertTrue(foundMenu.get().getIsDeleted());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should find menu by name for non-deleted menu")
    void testFindByNameIncludingDeleted_FindsNonDeleted() {
        Optional<Menu> foundMenu = menuRepo.findByNameIncludingDeleted("dashboard");

        Assertions.assertTrue(foundMenu.isPresent());
        Assertions.assertEquals("dashboard", foundMenu.get().getName());
        Assertions.assertFalse(foundMenu.get().getIsDeleted());
    }


    @Test
    @Order(5)
    @DisplayName("5. Should not find non-existent menu by name")
    void testFindByNameIncludingDeleted_NotFound() {
        Optional<Menu> foundMenu = menuRepo.findByNameIncludingDeleted("non-existent-menu");

        Assertions.assertFalse(foundMenu.isPresent());
    }

    @Test
    @Order(6)
    @DisplayName("6. Should find menu by code including deleted ones")
    void testFindByCodeIncludingDeleted_FindsDeleted() {
        Menu deletedMenu = createMenu("deletedMenuByCode", "/deleted", "deletedClass", "fa fa-trash", "DM2");
        deletedMenu.setIsDeleted(true);
        entityManager.persistAndFlush(deletedMenu);

        Optional<Menu> foundMenu = menuRepo.findByCodeIncludingDeleted("DM2");

        Assertions.assertTrue(foundMenu.isPresent());
        Assertions.assertEquals("DM2", foundMenu.get().getCode());
        Assertions.assertTrue(foundMenu.get().getIsDeleted());
    }

    @Test
    @Order(7)
    @DisplayName("7. Should find menu by code for non-deleted menu")
    void testFindByCodeIncludingDeleted_FindsNonDeleted() {
        Optional<Menu> foundMenu = menuRepo.findByCodeIncludingDeleted("DSH");

        Assertions.assertTrue(foundMenu.isPresent());
        Assertions.assertEquals("DSH", foundMenu.get().getCode());
        Assertions.assertFalse(foundMenu.get().getIsDeleted());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should not find non-existent menu by code")
    void testFindByCodeIncludingDeleted_NotFound() {
        Optional<Menu> foundMenu = menuRepo.findByCodeIncludingDeleted("non-existent-code");

        Assertions.assertFalse(foundMenu.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should find menus by parent ID")
    void testFindByParentId() {
        UUID parentId = UUID.randomUUID();
        Menu childMenu = createMenu("child", "/child", "childClass", "fa fa-child", "CH1");
        childMenu.setParentId(parentId);
        entityManager.persistAndFlush(childMenu);

        List<Menu> children = menuRepo.findByParentId(parentId);
        Assertions.assertEquals(1, children.size());
        Assertions.assertEquals("child", children.getFirst().getName());
    }

    @Test
    @Order(10)
    @DisplayName("10. Should fail to save menu with code longer than 3 characters")
    void testSaveMenu_CodeTooLong_ShouldFail() {
        Menu invalidMenu = createMenu("invalid", "/invalid", "invalidClass", "fa fa-exclamation", "LONGCODE");

        Assertions.assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            menuRepo.save(invalidMenu);
            entityManager.flush();
        });
    }
}
