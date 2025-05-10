package com.playus.userservice.global.response;


import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ErrorResponse(
        int code,
        HttpStatus status,
        String message
) {

    public static ErrorResponse badRequestError (String errorMessage) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    public static ErrorResponse unauthorizedError (String errorMessage) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, errorMessage);
    }

    public static ErrorResponse forbiddenError (String errorMessage) {
        return createErrorResponse(HttpStatus.FORBIDDEN, errorMessage);
    }

    public static ErrorResponse notFoundError(String errorMessage) {
        return createErrorResponse(HttpStatus.NOT_FOUND, errorMessage);
    }

    public static ErrorResponse internalServerError (String errorMessage) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    private static ErrorResponse createErrorResponse(HttpStatus errorStatus, String errorMessage) {
        return ErrorResponse.builder()
                .code(errorStatus.value())
                .status(errorStatus)
                .message(errorMessage)
                .build();
    }
}
