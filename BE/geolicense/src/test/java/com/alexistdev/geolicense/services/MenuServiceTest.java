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
import com.alexistdev.geolicense.models.repository.MenuRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepo menuRepo;

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
}
