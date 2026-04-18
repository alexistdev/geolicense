package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.UserDTO;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.services.UserService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final MessagesUtils messagesUtils;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, MessagesUtils messagesUtils, ModelMapper modelMapper) {
        this.userService = userService;
        this.messagesUtils = messagesUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    public ResponseEntity<ResponseData<Page<UserDTO>>> getAllUserData(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        ResponseData<Page<UserDTO>> responseData = new ResponseData<>();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> usersPage;

        try {
            usersPage = userService.getAllUsers(pageable);
        } catch (RuntimeException e) {
            Pageable fallbackPageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
            usersPage = userService.getAllUsers(fallbackPageable);
        }

        responseData.getMessages().add(this.messagesUtils.getMessage("usercontroller.user.nouser"));
        responseData.setStatus(false);

        handleNonEmptyPage(responseData, usersPage, page + 1);

        Page<UserDTO> userDTOS = usersPage
                .map(user -> modelMapper.map(user, UserDTO.class));
        responseData.setPayload(userDTOS);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    private <T> void handleNonEmptyPage(ResponseData<Page<T>> responseData, Page<?> pageResult, int pageNumber) {
        if (!pageResult.isEmpty()) {
            responseData.setStatus(true);
            if (!responseData.getMessages().isEmpty()) {
                responseData.getMessages().removeFirst();
            }
            responseData.getMessages().add("Retrieved page " + pageNumber + " of products");
        }
    }
}
