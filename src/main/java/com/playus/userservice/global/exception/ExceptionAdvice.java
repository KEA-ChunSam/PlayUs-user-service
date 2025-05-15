package com.playus.userservice.global.exception;

import com.playus.userservice.domain.oauth.controller.AuthController;
import com.playus.userservice.domain.oauth.service.CustomOAuth2UserService;
import com.playus.userservice.domain.user.controller.ProfileSetupController;
import com.playus.userservice.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        AuthController.class,
        CustomOAuth2UserService.class,
        ProfileSetupController.class
})
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindExceptionHandler(BindException e) {
        String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation Error: {}", errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse responseStatusExHandler(ResponseStatusException e) {
        String errorMessage = e.getMessage();
        log.warn("INVALID_TOKEN Error: {}", errorMessage);
        return ErrorResponse.unauthorizedError(errorMessage);
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> oauth2AuthExceptionHandler(OAuth2AuthenticationException e) {
        String errorCode = e.getError().getErrorCode();
        HttpStatus status = switch (errorCode) {
            case "missing_phone", "invalid_gender", "unsupported_provider" -> HttpStatus.BAD_REQUEST;
            case "provider_mismatch" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNAUTHORIZED;
        };

        ErrorResponse body = ErrorResponse.builder()
                .code(status.value())
                .status(status)
                .message(e.getError().getDescription())
                .build();

        log.warn("OAuth2 Error ({}): {}", errorCode, body.message());
        return ResponseEntity.status(status).body(body);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> otherExceptionHandler(Exception e) {
        String errorMessage = e.getMessage();
        log.error("Unexpected Error: {}", errorMessage);
        return ResponseEntity.internalServerError().body("서버 에러가 발생했습니다! 관리자에게 문의해 주세요!");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse body = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST)
                .message(errorMessage)
                .build();
        log.warn("Validation Error: {}", errorMessage);
        return ResponseEntity.badRequest().body(body);
    }

}
