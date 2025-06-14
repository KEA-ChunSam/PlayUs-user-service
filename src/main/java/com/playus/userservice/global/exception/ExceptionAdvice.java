package com.playus.userservice.global.exception;

import com.playus.userservice.domain.notification.controller.NotificationApiController;
import com.playus.userservice.domain.notification.controller.NotificationController;
import com.playus.userservice.domain.oauth.controller.AuthController;
import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.oauth.service.CustomOAuth2UserService;
import com.playus.userservice.domain.user.controller.*;
import com.playus.userservice.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        AuthController.class,
        CustomOAuth2UserService.class,
        ProfileSetupController.class,
        FavoriteTeamController.class,
        UserProfileController.class,
        UserController.class,
        PartyUserController.class,
        UserReviewController.class,
        UserTagReadController.class,
        NotificationController.class,
        NotificationApiController.class
})
public class ExceptionAdvice {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindExceptionHandler(BindException e) {
        String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
        log.error("Validation Error: {}", errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        HttpStatusCode statusCode = e.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String message = e.getReason();
        ErrorResponse body = ErrorResponse.builder()
                .code(status.value())
                .status(status)
                .message(message)
                .build();
        log.error("API Error ({}): {}", status, message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Void> handleAsyncTimeout(AsyncRequestTimeoutException e) {
        log.warn("AsyncRequestTimeoutException 발생 – notification: {}", e.getMessage());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        Object invalidValue = ex.getValue();
        String message = String.format("'%s'는 숫자여야 합니다: %s", paramName, invalidValue);
        ErrorResponse body = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();
        log.error("Type Mismatch ({}={}): {}", paramName, invalidValue, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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
        log.error("OAuth2 Error ({}): {}", errorCode, body.message());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String message = "잘못된 요청 형식입니다.";
        ErrorResponse body = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();
        log.error("JSON parse error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> otherExceptionHandler(Exception e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            log.error("Unexpected Error for user {}: {}", user.getName(), e.getMessage(), e);
        } else {
            log.error("Unexpected Error: {}", e.getMessage(), e);
        }

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
        log.error("Validation Error: {}", errorMessage);
        return ResponseEntity.badRequest().body(body);
    }
}
