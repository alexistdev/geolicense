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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class MenuService {

    private final MenuRepo menuRepo;
    private static final Logger logger = Logger.getLogger(MenuService.class.getName());
    private static final String SYSTEM_USER = "System";
    private final MessagesUtils messagesUtils;

    public MenuService(MenuRepo menuRepo, MessagesUtils messagesUtils) {
        this.menuRepo = menuRepo;
        this.messagesUtils = messagesUtils;
    }

    public MenuResponse addMenu(MenuRequest request){
        Optional<Menu> foundMenu = menuRepo.findByNameIncludingDeleted(request.getName());

        Menu menuToSave;
        menuToSave = convertToMenu(request, null);
        if(foundMenu.isPresent()){
            Menu existingMenu = foundMenu.get();
            if(!existingMenu.getIsDeleted()){
                String message = messagesUtils.getMessage("menu.already.exist", request.getName());
                logger.warning(message);
                throw new ExistingException(message);
            }
            menuToSave = convertToMenu(request, existingMenu.getId());
            menuToSave.setDeleted(false);
        }
        Menu savedMenu = menuRepo.save(menuToSave);
        return MenuResponse.builder()
                .id(savedMenu.getId().toString())
                .name(savedMenu.getName())
                .urlink(savedMenu.getUrlink())
                .classlink(savedMenu.getClasslink())
                .icon(savedMenu.getIcon())
                .sortOrder(String.valueOf(savedMenu.getSortOrder()))
                .parentId(savedMenu.getParentId() != null ? savedMenu.getParentId().toString() : null)
                .typeMenu(savedMenu.getTypeMenu())
                .code(savedMenu.getCode())
                .build();

    }

    private Menu convertToMenu(MenuRequest request, UUID id) {
        Menu menu = new Menu();
        if(id != null) menu.setId(id);
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
}
