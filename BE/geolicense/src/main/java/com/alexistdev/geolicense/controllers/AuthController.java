package com.alexistdev.geolicense.controllers;

import com.alexistdev.geolicense.dto.RegisterRequestDTO;
import com.alexistdev.geolicense.dto.ResponseData;
import com.alexistdev.geolicense.dto.response.AuthRegisterDTO;
import com.alexistdev.geolicense.services.AuthService;
import com.alexistdev.geolicense.utils.MessagesUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MessagesUtils messagesUtils;
    private final RateLimiter rateLimiter;
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    public AuthController(AuthService authService, MessagesUtils messagesUtils) {
        this.authService = authService;
        this.messagesUtils = messagesUtils;

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(3)
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        this.rateLimiter = registry.rateLimiter("rateLimiter");
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseData<AuthRegisterDTO>> register(@Valid
            @RequestBody RegisterRequestDTO request, Errors errors
    ) {
        Supplier<ResponseEntity<ResponseData<AuthRegisterDTO>>> registerAttempt = () -> {
            ResponseData<AuthRegisterDTO> responseData = new ResponseData<>();
            handleErrors(errors, responseData);

            if (!responseData.isStatus()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }

            responseData.setPayload(authService.register(request));
            String msgSuccess = messagesUtils.getMessage("authcontroller.register.success");
            responseData.getMessages().add(msgSuccess);
            responseData.setStatus(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        };

        Supplier<ResponseEntity<ResponseData<AuthRegisterDTO>>> restrictedRegisterAttempt = RateLimiter
                .decorateSupplier(rateLimiter,registerAttempt);
        try{
            return restrictedRegisterAttempt.get();
        } catch (RequestNotPermitted e) {
            String msgError = messagesUtils.getMessage("authcontroller.register.ratelimit");
            logger.warning(msgError);
            ResponseData<AuthRegisterDTO> responseData = new ResponseData<>();
            responseData.getMessages().add(msgError);
            responseData.setStatus(false);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(responseData);
        }
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
