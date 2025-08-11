package com.allinone.DevView.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 통일된 API 에러 응답 형식
 * 모든 에러 응답을 일관된 구조로 제공
 */
@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private final int status;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;

    /**
     * 완전한 에러 응답 생성
     *
     * @param httpStatus HTTP 상태
     * @param message 에러 메시지
     * @param errorCode 에러 코드
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(HttpStatus httpStatus, String message, String errorCode) {
        return ErrorResponse.builder()
                .status(httpStatus.value())
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 메시지와 에러 코드로 에러 응답 생성 (기본: BAD_REQUEST)
     *
     * @param message 에러 메시지
     * @param errorCode 에러 코드
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(String message, String errorCode) {
        return of(HttpStatus.BAD_REQUEST, message, errorCode);
    }

    /**
     * 메시지만으로 에러 응답 생성 (기본: UNKNOWN_ERROR)
     *
     * @param message 에러 메시지
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(String message) {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, message, "UNKNOWN_ERROR");
    }

    /**
     * HTTP 상태만으로 기본 에러 응답 생성
     *
     * @param httpStatus HTTP 상태
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(HttpStatus httpStatus) {
        return ErrorResponse.builder()
                .status(httpStatus.value())
                .message(httpStatus.getReasonPhrase())
                .errorCode(httpStatus.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
}