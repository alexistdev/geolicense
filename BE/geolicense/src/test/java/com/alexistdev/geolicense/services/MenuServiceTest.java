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
import com.alexistdev.geolicense.exceptions.ExistingException;
import com.alexistdev.geolicense.models.entity.Menu;
import com.alexistdev.geolicense.models.repository.MenuRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("Should save a new menu when it does not exist")
    void addMenu_WhenMenuDoesNotExist_ShouldSaveAndReturnResponse() {
        when(menuRepo.findByNameIncludingDeleted(menuRequest.getName())).thenReturn(Optional.empty());
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);

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

        verify(menuRepo, times(1)).findByNameIncludingDeleted(menuRequest.getName());
        verify(menuRepo, times(1)).save(any(Menu.class));
    }

    @Test
    @Order(2)
    @DisplayName("Should update existing menu when it was previously deleted")
    void addMenu_WhenMenuExistsAndIsDeleted_ShouldUpdateAndReturnResponse() {
        Menu existingDeletedMenu = new Menu();
        existingDeletedMenu.setId(menuId);
        existingDeletedMenu.setName(menuRequest.getName());
        existingDeletedMenu.setDeleted(true);

        when(menuRepo.findByNameIncludingDeleted(menuRequest.getName()))
                .thenReturn(Optional.of(existingDeletedMenu));
        when(menuRepo.save(any(Menu.class))).thenReturn(menu);

        MenuResponse response = menuService.addMenu(menuRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(menuId.toString(), response.getId());
        Assertions.assertEquals(menuRequest.getName(), response.getName());

        verify(menuRepo, times(1)).findByNameIncludingDeleted(menuRequest.getName());
        verify(menuRepo, times(1)).save(any(Menu.class));
    }

    @Test
    @Order(3)
    @DisplayName("Should throw ExistingException when menu already exists and is not deleted")
    void addMenu_WhenMenuAlreadyExists_ShouldThrowExistingException() {
        Menu existingActiveMenu = new Menu();
        existingActiveMenu.setId(menuId);
        existingActiveMenu.setName(menuRequest.getName());
        existingActiveMenu.setDeleted(false);

        when(menuRepo.findByNameIncludingDeleted(menuRequest.getName()))
                .thenReturn(Optional.of(existingActiveMenu));

        String errorMessage = "Menu already exists.";
        when(messagesUtils.getMessage(anyString(), any())).thenReturn(errorMessage);

        ExistingException exception = assertThrows(ExistingException.class,
                () -> menuService.addMenu(menuRequest));

        Assertions.assertEquals(errorMessage, exception.getMessage());

        verify(menuRepo, times(1)).findByNameIncludingDeleted(menuRequest.getName());
        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(4)
    @DisplayName("Should throw IllegalArgumentException when parentId is not a valid UUID")
    void addMenu_WhenParentIdIsInvalid_ShouldThrowIllegalArgumentException() {
        menuRequest.setParentId("invalid-uuid");
        when(menuRepo.findByNameIncludingDeleted(menuRequest.getName())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }

    @Test
    @Order(5)
    @DisplayName("Should throw NumberFormatException when sortOrder is not numeric")
    void addMenu_WhenSortOrderIsInvalid_ShouldThrowNumberFormatException() {
        menuRequest.setSortOrder("not-a-number");
        when(menuRepo.findByNameIncludingDeleted(menuRequest.getName())).thenReturn(Optional.empty());

        assertThrows(NumberFormatException.class,
                () -> menuService.addMenu(menuRequest));

        verify(menuRepo, never()).save(any(Menu.class));
    }
}
