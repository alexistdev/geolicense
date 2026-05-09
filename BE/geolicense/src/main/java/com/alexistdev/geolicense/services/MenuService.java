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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MenuService {

    private final MenuRepo menuRepo;
    private final RoleMenuRepo roleMenuRepo;
    private static final Logger logger = Logger.getLogger(MenuService.class.getName());
    private static final String SYSTEM_USER = "System";
    private final MessagesUtils messagesUtils;

    public MenuService(MenuRepo menuRepo, RoleMenuRepo roleMenuRepo, MessagesUtils messagesUtils) {
        this.menuRepo = menuRepo;
        this.roleMenuRepo = roleMenuRepo;
        this.messagesUtils = messagesUtils;
    }

    public List<MenuResponse> getMenusByRole(Role role) {
        return roleMenuRepo.findByRole(role).stream()
                .map(RoleMenu::getMenu)
                .map(this::convertToMenuResponse)
                .collect(Collectors.toList());
    }

    private MenuResponse convertToMenuResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId().toString())
                .name(menu.getName())
                .urlink(menu.getUrlink())
                .classlink(menu.getClasslink())
                .icon(menu.getIcon())
                .sortOrder(String.valueOf(menu.getSortOrder()))
                .parentId(menu.getParentId() != null ? menu.getParentId().toString() : null)
                .typeMenu(menu.getTypeMenu())
                .code(menu.getCode())
                .build();
    }


    @Transactional
    public MenuResponse addMenu(MenuRequest request){
        Menu savedMenu = menuRepo.save(convertToMenu(request));
        String messageSuccess = messagesUtils.getMessage("menu.add.success");
        logger.info(messageSuccess);
        return this.convertToMenuResponse(savedMenu);
    }

    private Menu convertToMenu(MenuRequest request) {
        Menu menu = new Menu();
        menu.setName(request.getName());
        menu.setUrlink(request.getUrlink());
        menu.setClasslink(request.getClasslink());
        menu.setSortOrder(Integer.parseInt(request.getSortOrder()));
        menu.setIcon(request.getIcon());
        menu.setTypeMenu(request.getTypeMenu());
        menu.setCode(request.getCode());
        if (request.getParentId() != null) menu.setParentId(UUID.fromString(request.getParentId()));
        menu.setCreatedBy(SYSTEM_USER);
        menu.setModifiedBy(SYSTEM_USER);
        menu.setCreatedDate(new java.util.Date());
        menu.setModifiedDate(new java.util.Date());
        return menu;
    }

    public Menu findByCode(String code){
        Optional<Menu> foundMenu = menuRepo.findByCodeIncludingDeleted(code);
        return foundMenu.orElse(null);
    }
}
