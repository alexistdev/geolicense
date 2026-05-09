/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.repository;

import com.alexistdev.geolicense.models.entity.Menu;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.RoleMenu;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class RoleMenuRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleMenuRepo roleMenuRepo;

    private static final String SYSTEM_USER = "System";

    private Menu createMenu(String url, int typeMenu, String code) {
        Menu menu = new Menu();
        menu.setName("Dashboard");
        menu.setUrlink(url);
        menu.setClasslink("menu-title d-flex align-items-center");
        menu.setIsDeleted(false);
        menu.setCreatedBy(SYSTEM_USER);
        menu.setModifiedBy(SYSTEM_USER);
        menu.setParentId(null);
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        menu.setSortOrder(1);
        menu.setTypeMenu(typeMenu);
        menu.setIcon("bx bx-home-alt");
        menu.setCode(code);
        return menu;
    }

    private RoleMenu createRoleMenu(Role role, Menu menu) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(role);
        roleMenu.setMenu(menu);
        roleMenu.setCreatedBy("system");
        roleMenu.setCreatedDate(new java.util.Date());
        roleMenu.setModifiedBy("system");
        roleMenu.setModifiedDate(new java.util.Date());
        roleMenu.setIsDeleted(false);
        return roleMenu;
    }

    @Test
    @Order(1)
    @DisplayName("1. Test Save RoleMenu")
    void testSaveRoleMenu() {
        Menu menu = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        RoleMenu savedRoleMenu = roleMenuRepo.save(roleMenu);

        Assertions.assertNotNull(savedRoleMenu);
        Assertions.assertEquals(roleMenu.getRole(),savedRoleMenu.getRole());
        Assertions.assertEquals(roleMenu.getMenu(),savedRoleMenu.getMenu());
        Assertions.assertEquals(roleMenu.getCreatedBy(),savedRoleMenu.getCreatedBy());
        Assertions.assertEquals(roleMenu.getCreatedDate(),savedRoleMenu.getCreatedDate());
        Assertions.assertEquals(roleMenu.getModifiedBy(),savedRoleMenu.getModifiedBy());
        Assertions.assertEquals(roleMenu.getModifiedDate(),savedRoleMenu.getModifiedDate());
        Assertions.assertFalse(savedRoleMenu.getIsDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test Find By RoleMenu UUID")
    void testFindByRoleMenuUUID(){
        Menu menu = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        Optional<RoleMenu> foundRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertTrue(foundRoleMenu.isPresent());
        Assertions.assertEquals(roleMenu.getRole(),foundRoleMenu.get().getRole());
        Assertions.assertEquals(roleMenu.getMenu(),foundRoleMenu.get().getMenu());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test Find All RoleMenu")
    void testFindAllRoleMenu() {
        Menu menu = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu1 = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu1);
        entityManager.flush();

        RoleMenu roleMenu2 = createRoleMenu(Role.USER,menu);
        entityManager.persist(roleMenu2);
        entityManager.flush();

        List<RoleMenu> allRoleMenu = roleMenuRepo.findAll();
        Assertions.assertEquals(2,allRoleMenu.size());
    }

    @Test
    @Order(4)
    @DisplayName("4. Test Delete RoleMenu")
    void testDeleteRoleMenu() {
        Menu menu = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);
        entityManager.flush();

        roleMenuRepo.delete(roleMenu);
        Optional<RoleMenu> deletedRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertFalse(deletedRoleMenu.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("5. Test Delete All RoleMenu")
    void testDeleteAllRoleMenu() {
        Menu menu1 = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu1);
        entityManager.flush();

        Menu menu2 = createMenu("/users/dashboard", 2,"us1");
        entityManager.persist(menu2);
        entityManager.flush();

        RoleMenu roleMenu1 = createRoleMenu(Role.ADMIN,menu1);
        entityManager.persist(roleMenu1);
        entityManager.flush();

        RoleMenu roleMenu2 = createRoleMenu(Role.USER,menu2);
        entityManager.persist(roleMenu2);
        entityManager.flush();

        roleMenuRepo.deleteAll();
        List<RoleMenu> allRoleMenu = roleMenuRepo.findAll();
        Assertions.assertEquals(0,allRoleMenu.size());
    }

    @Test
    @Order(6)
    @DisplayName("6. Test Update Role Menu")
    void testUpdateRoleMenu() {
        Menu menu = createMenu("/admin/dashboard", 1, "ad1");
        entityManager.persist(menu);
        entityManager.flush();

        RoleMenu roleMenu = createRoleMenu(Role.ADMIN,menu);
        entityManager.persist(roleMenu);

        roleMenu.setRole(Role.USER);
        roleMenuRepo.save(roleMenu);

        Optional<RoleMenu> updatedRoleMenu = roleMenuRepo.findById(roleMenu.getId());
        Assertions.assertTrue(updatedRoleMenu.isPresent());
        Assertions.assertEquals(Role.USER,updatedRoleMenu.get().getRole());
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Serializable")
    void testSerializable() {
        Assertions.assertInstanceOf(Serializable.class,roleMenuRepo);
    }

    @Test
    @Order(8)
    @DisplayName("8. Test RoleMenu Entity")
    void testRoleMenuEntity() {
        Assertions.assertInstanceOf(RoleMenu.class,new RoleMenu());
    }

    @Test
    @Order(9)
    @DisplayName("9. Test RoleMenu Repo")
    void testMenuEntity() {
        Assertions.assertInstanceOf(RoleMenuRepo.class,roleMenuRepo);
    }

}
