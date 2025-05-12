package com.playus.userservice.global.exception;

import com.playus.userservice.domain.oauth.controller.TokenController;
import com.playus.userservice.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        TokenController.class
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

    /*
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            PartyGenderExceptionGroup.InvalidDescriptionException.class,
            PartyJoinMethodExceptionGroup.InvalidDescriptionException.class,
            PartyAgeGroupExceptionGroup.InvalidDescriptionException.class
    })
    public ResponseEntity<String> invalidDescriptionExceptionHandler(Exception e) {
        String errorMessage = e.getMessage();
        log.warn("Validation Error: {}", errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }
     */

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> otherExceptionHandler(Exception e) {
        String errorMessage = e.getMessage();
        log.error("Unexpected Error: {}", errorMessage);
        return ResponseEntity.internalServerError().body("서버 에러가 발생했습니다! 관리자에게 문의해 주세요!");
    }
}
