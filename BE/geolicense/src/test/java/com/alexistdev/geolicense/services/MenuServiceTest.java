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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @DisplayName("5. Should return list of MenuResponse mapped from RoleMenus when role has menus")
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
    @Order(6)
    @DisplayName("6. Should return empty list when role has no menus")
    void getMenusByRole_WhenRoleHasNoMenus_ShouldReturnEmptyList() {
        when(roleMenuRepo.findByRole(Role.USER)).thenReturn(List.of());

        List<MenuResponse> result = menuService.getMenusByRole(Role.USER);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(roleMenuRepo, times(1)).findByRole(Role.USER);
    }

    @Test
    @Order(7)
    @DisplayName("7. Should correctly map all menu fields to MenuResponse")
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
}
