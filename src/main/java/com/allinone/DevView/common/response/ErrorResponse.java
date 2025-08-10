package com.allinone.DevView.common.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 에러 응답 형식
 * 모든 에러 응답을 통일된 형태로 제공
 */
@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private final int status;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;

    /**
     * 기본 에러 응답 생성
     *
     * @param message 에러 메시지
     * @param errorCode 에러 코드
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(String message, String errorCode) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 메시지만으로 에러 응답 생성
     *
     * @param message 에러 메시지
     * @return ErrorResponse 에러 응답 객체
     */
    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("UNKNOWN_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }
}