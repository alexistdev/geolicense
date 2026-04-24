package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.RegisterRequestDTO;
import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.response.AuthRegisterDTO;
import com.alexistdev.geolicense.services.AuthService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessagesUtils messagesUtils;
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    @PostMapping("/register")
    public ResponseEntity<ResponseData<AuthRegisterDTO>> register(@Valid
            @RequestBody RegisterRequestDTO request, Errors errors
    ) {
        ResponseData<AuthRegisterDTO> responseData = new ResponseData<>();
        handleErrors(errors,responseData);

        if(!responseData.isStatus()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }

        responseData.setPayload(authService.register(request));
        String msgSuccess = messagesUtils.getMessage("authcontroller.register.success");
        responseData.getMessages().add(msgSuccess);
        responseData.setStatus(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

//    @PostMapping("/login")
//    public ResponseEntity<AuthResponseDTO> authenticate(
//            @RequestBody AuthRequestDTO request
//    ) {
//        return ResponseEntity.ok(service.authenticate(request));
//    }

    private void handleErrors(Errors errors, ResponseData<?> responseData) {
        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                logger.info(error.getDefaultMessage());
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
        } else {
            responseData.setStatus(true);
        }
    }
}
