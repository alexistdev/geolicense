package com.alexistdev.geolicense.services;

import com.alexistdev.geolicense.dto.UserDTO;
import com.alexistdev.geolicense.dto.response.UserResponse;
import com.alexistdev.geolicense.exceptions.NotFoundException;
import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.models.entity.User;
import com.alexistdev.geolicense.models.repository.UserRepo;
import com.alexistdev.geolicense.utils.MessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public UserService(UserRepo userRepo, BCryptPasswordEncoder bCryptPasswordEncoder, MessagesUtils messagesUtils) {
        this.userRepo = userRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.messagesUtils = messagesUtils;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepo.findByRoleNot(Role.ADMIN, pageable);
    }

    public UserResponse findUserById(String id) {
        UUID userId = UUID.fromString(id);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        messagesUtils.getMessage("userservice.user.notfound", id)));
        return convertToUserDTO(user);
    }

    private UserResponse convertToUserDTO(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId().toString());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        assert user.getRole() != null;
        userResponse.setRole(user.getRole().toString());
        userResponse.setSuspended(user.isSuspended());
        userResponse.setCreatedDate(user.getCreatedDate());
        userResponse.setModifiedDate(user.getModifiedDate());
        return userResponse;
    }

}
