package com.playus.userservice.global.response;

public record TokenValidationResponse(
        boolean blacklisted
) {}
