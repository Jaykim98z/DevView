package com.allinone.DevView.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "Server Error"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "User not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-002", "Email already exists"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-003", "Username already exists"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER-004", "Login failed"),

    // Interview
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW-001", "Interview not found");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
