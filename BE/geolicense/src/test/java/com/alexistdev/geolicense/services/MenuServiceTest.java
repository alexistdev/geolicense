/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.request.MenuRequest;
import com.alexistdev.geolicense.dto.response.MenuResponse;
import com.alexistdev.geolicense.models.entity.Menu;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.RoleMenu;
import com.alexistdev.geolicense.models.repository.MenuRepo;
import com.alexistdev.geolicense.models.repository.RoleMenuRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepo menuRepo;

    @Mock
    private RoleMenuRepo roleMenuRepo;

    @Mock
    private MessagesUtils messagesUtils;

    @InjectMocks
    private MenuService menuService;

    private MenuRequest menuRequest;
    private Menu menu;
    private UUID menuId;
    private UUID parentId;

    @BeforeEach
    void setUp() {
        menuId = UUID.randomUUID();
        parentId = UUID.randomUUID();

        menuRequest = MenuRequest.builder()
                .name("dashboard")
                .classlink("dashboardClass")
                .urlink("/dashboard")
                .typeMenu(1)
                .code("MN12")
                .icon("fa fa-fontawesome")
                .parentId(parentId.toString())
                .sortOrder("1")
                .build();

        menu = new Menu();
        menu.setId(menuId);
        menu.setName(menuRequest.getName());
        menu.setTypeMenu(menuRequest.getTypeMenu());
        menu.setUrlink(menuRequest.getUrlink());
        menu.setClasslink(menuRequest.getClasslink());
        menu.setParentId(parentId);
        menu.setIcon(menuRequest.getIcon());
        menu.setCode(menuRequest.getCode());
        menu.setSortOrder(Integer.parseInt(menuRequest.getSortOrder()));
    }

    // ─── addMenu ────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. Should save menu and return mapped MenuResponse when request is valid with parentId")
    void addMenu_WhenRequestIsValid_ShouldReturnMenuResponse() {
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);
        when(messagesUtils.getMessage("menu.add.success")).thenReturn("Menu added successfully");

        MenuResponse response = menuService.addMenu(menuRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(menuId.toString(), response.getId());
        Assertions.assertEquals(menuRequest.getName(), response.getName());
        Assertions.assertEquals(menuRequest.getUrlink(), response.getUrlink());
        Assertions.assertEquals(menuRequest.getClasslink(), response.getClasslink());
        Assertions.assertEquals(menuRequest.getIcon(), response.getIcon());
        Assertions.assertEquals(menuRequest.getSortOrder(), response.getSortOrder());
        Assertions.assertEquals(parentId.toString(), response.getParentId());
        Assertions.assertEquals(menuRequest.getTypeMenu(), response.getTypeMenu());
        Assertions.assertEquals(menuRequest.getCode(), response.getCode());
        verify(menuRepo, times(1)).save(any(Menu.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Should return MenuResponse with null parentId when parentId is not provided")
    void addMenu_WhenParentIdIsNull_ShouldReturnMenuResponseWithNullParentId() {
        menuRequest.setParentId(null);
        menu.setParentId(null);
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);
        when(messagesUtils.getMessage("menu.add.success")).thenReturn("Menu added successfully");

        MenuResponse response = menuService.addMenu(menuRequest);

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getParentId());
        verify(menuRepo, times(1)).save(any(Menu.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should throw IllegalArgumentException when parentId is not a valid UUID")
    void addMenu_WhenParentIdIsInvalid_ShouldThrowIllegalArgumentException() {
        menuRequest.setParentId("invalid-uuid");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(4)
    @DisplayName("4. Should throw NumberFormatException when sortOrder is not numeric")
    void addMenu_WhenSortOrderIsInvalid_ShouldThrowNumberFormatException() {
        menuRequest.setSortOrder("not-a-number");

        Assertions.assertThrows(NumberFormatException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(5)
    @DisplayName("5. Should throw NumberFormatException when sortOrder is empty string")
    void addMenu_WhenSortOrderIsEmpty_ShouldThrowNumberFormatException() {
        menuRequest.setSortOrder("");

        Assertions.assertThrows(NumberFormatException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(6)
    @DisplayName("6. Should throw IllegalArgumentException when parentId is empty string")
    void addMenu_WhenParentIdIsEmptyString_ShouldThrowIllegalArgumentException() {
        menuRequest.setParentId("");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(7)
    @DisplayName("7. Should pass correctly mapped entity fields to repository on save")
    void addMenu_ShouldPassCorrectlyMappedEntityToRepository() {
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);
        when(messagesUtils.getMessage("menu.add.success")).thenReturn("Menu added successfully");

        menuService.addMenu(menuRequest);

        ArgumentCaptor<Menu> menuCaptor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepo).save(menuCaptor.capture());

        Menu captured = menuCaptor.getValue();
        Assertions.assertEquals(menuRequest.getName(), captured.getName());
        Assertions.assertEquals(menuRequest.getUrlink(), captured.getUrlink());
        Assertions.assertEquals(menuRequest.getClasslink(), captured.getClasslink());
        Assertions.assertEquals(Integer.parseInt(menuRequest.getSortOrder()), captured.getSortOrder());
        Assertions.assertEquals(menuRequest.getIcon(), captured.getIcon());
        Assertions.assertEquals(menuRequest.getTypeMenu(), captured.getTypeMenu());
        Assertions.assertEquals(menuRequest.getCode(), captured.getCode());
        Assertions.assertEquals(UUID.fromString(menuRequest.getParentId()), captured.getParentId());
        Assertions.assertEquals("System", captured.getCreatedBy());
        Assertions.assertEquals("System", captured.getModifiedBy());
        Assertions.assertNotNull(captured.getCreatedDate());
        Assertions.assertNotNull(captured.getModifiedDate());
    }

    @Test
    @Order(8)
    @DisplayName("8. Should set null parentId on entity when parentId is not provided")
    void addMenu_WhenParentIdIsNull_ShouldSaveEntityWithNullParentId() {
        menuRequest.setParentId(null);
        menu.setParentId(null);
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);
        when(messagesUtils.getMessage("menu.add.success")).thenReturn("Menu added successfully");

        menuService.addMenu(menuRequest);

        ArgumentCaptor<Menu> menuCaptor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepo).save(menuCaptor.capture());

        Assertions.assertNull(menuCaptor.getValue().getParentId());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should call MessagesUtils with correct key on addMenu success")
    void addMenu_ShouldCallMessagesUtilsWithSuccessKey() {
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);
        when(messagesUtils.getMessage("menu.add.success")).thenReturn("Menu added successfully");

        menuService.addMenu(menuRequest);

        verify(messagesUtils, times(1)).getMessage("menu.add.success");
    }

    // ─── getMenusByRole ──────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("10. Should return list of MenuResponse mapped from RoleMenus when role has menus")
    void getMenusByRole_WhenRoleHasMenus_ShouldReturnMenuResponseList() {
        RoleMenu roleMenu1 = new RoleMenu();
        roleMenu1.setRole(Role.ADMIN);
        roleMenu1.setMenu(menu);

        Menu menu2 = new Menu();
        menu2.setId(UUID.randomUUID());
        menu2.setName("settings");
        menu2.setUrlink("/settings");
        menu2.setClasslink("settingsClass");
        menu2.setIcon("fa fa-cog");
        menu2.setSortOrder(2);
        menu2.setTypeMenu(1);
        menu2.setCode("MN13");

        RoleMenu roleMenu2 = new RoleMenu();
        roleMenu2.setRole(Role.ADMIN);
        roleMenu2.setMenu(menu2);

        when(roleMenuRepo.findByRole(Role.ADMIN)).thenReturn(List.of(roleMenu1, roleMenu2));

        List<MenuResponse> result = menuService.getMenusByRole(Role.ADMIN);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals(menuId.toString(), result.getFirst().getId());
        Assertions.assertEquals(menu.getName(), result.getFirst().getName());
        Assertions.assertEquals(menu.getUrlink(), result.get(0).getUrlink());
        Assertions.assertEquals(parentId.toString(), result.get(0).getParentId());

        Assertions.assertEquals(menu2.getId().toString(), result.get(1).getId());
        Assertions.assertEquals(menu2.getName(), result.get(1).getName());
        Assertions.assertNull(result.get(1).getParentId());

        verify(roleMenuRepo, times(1)).findByRole(Role.ADMIN);
    }

    @Test
    @Order(11)
    @DisplayName("11. Should return empty list when role has no menus")
    void getMenusByRole_WhenRoleHasNoMenus_ShouldReturnEmptyList() {
        when(roleMenuRepo.findByRole(Role.USER)).thenReturn(List.of());

        List<MenuResponse> result = menuService.getMenusByRole(Role.USER);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(roleMenuRepo, times(1)).findByRole(Role.USER);
    }

    @Test
    @Order(12)
    @DisplayName("12. Should correctly map all menu fields to MenuResponse")
    void getMenusByRole_ShouldMapAllMenuFieldsCorrectly() {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRole(Role.USER);
        roleMenu.setMenu(menu);

        when(roleMenuRepo.findByRole(Role.USER)).thenReturn(List.of(roleMenu));

        List<MenuResponse> result = menuService.getMenusByRole(Role.USER);

        Assertions.assertEquals(1, result.size());
        MenuResponse response = result.getFirst();

        Assertions.assertEquals(menuId.toString(), response.getId());
        Assertions.assertEquals(menuRequest.getName(), response.getName());
        Assertions.assertEquals(menuRequest.getUrlink(), response.getUrlink());
        Assertions.assertEquals(menuRequest.getClasslink(), response.getClasslink());
        Assertions.assertEquals(menuRequest.getIcon(), response.getIcon());
        Assertions.assertEquals(menuRequest.getSortOrder(), response.getSortOrder());
        Assertions.assertEquals(parentId.toString(), response.getParentId());
        Assertions.assertEquals(menuRequest.getTypeMenu(), response.getTypeMenu());
        Assertions.assertEquals(menuRequest.getCode(), response.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("13. Should return different menus for ADMIN and USER roles")
    void getMenusByRole_ShouldReturnDifferentMenusForDifferentRoles() {
        RoleMenu adminRoleMenu = new RoleMenu();
        adminRoleMenu.setRole(Role.ADMIN);
        adminRoleMenu.setMenu(menu);

        Menu userMenu = new Menu();
        userMenu.setId(UUID.randomUUID());
        userMenu.setName("user-dashboard");
        userMenu.setUrlink("/user/dashboard");
        userMenu.setCode("MN_USER");
        userMenu.setTypeMenu(1);
        userMenu.setSortOrder(1);

        RoleMenu userRoleMenu = new RoleMenu();
        userRoleMenu.setRole(Role.USER);
        userRoleMenu.setMenu(userMenu);

        when(roleMenuRepo.findByRole(Role.ADMIN)).thenReturn(List.of(adminRoleMenu));
        when(roleMenuRepo.findByRole(Role.USER)).thenReturn(List.of(userRoleMenu));

        List<MenuResponse> adminResult = menuService.getMenusByRole(Role.ADMIN);
        List<MenuResponse> userResult = menuService.getMenusByRole(Role.USER);

        Assertions.assertEquals(1, adminResult.size());
        Assertions.assertEquals(1, userResult.size());
        Assertions.assertNotEquals(adminResult.getFirst().getId(), userResult.getFirst().getId());
        Assertions.assertEquals(menu.getName(), adminResult.getFirst().getName());
        Assertions.assertEquals(userMenu.getName(), userResult.getFirst().getName());

        verify(roleMenuRepo, times(1)).findByRole(Role.ADMIN);
        verify(roleMenuRepo, times(1)).findByRole(Role.USER);
    }

    // ─── findByCode ──────────────────────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("14. Should return menu when code exists")
    void findByCode_WhenCodeExists_ShouldReturnMenu() {
        when(menuRepo.findByCodeIncludingDeleted("MN12")).thenReturn(Optional.of(menu));

        Menu result = menuService.findByCode("MN12");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(menuId, result.getId());
        Assertions.assertEquals(menu.getName(), result.getName());
        Assertions.assertEquals(menu.getCode(), result.getCode());
        verify(menuRepo, times(1)).findByCodeIncludingDeleted("MN12");
    }

    @Test
    @Order(15)
    @DisplayName("15. Should return null when code does not exist")
    void findByCode_WhenCodeDoesNotExist_ShouldReturnNull() {
        when(menuRepo.findByCodeIncludingDeleted("UNKNOWN")).thenReturn(Optional.empty());

        Menu result = menuService.findByCode("UNKNOWN");

        Assertions.assertNull(result);
        verify(menuRepo, times(1)).findByCodeIncludingDeleted("UNKNOWN");
    }
}
