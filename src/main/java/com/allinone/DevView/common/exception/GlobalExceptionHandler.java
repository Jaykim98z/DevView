package com.allinone.DevView.common.exception;

import com.allinone.DevView.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 애플리케이션에서 발생하는 모든 예외를 일관성 있게 처리
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 (잘못된 요청)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("비즈니스 로직 오류: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                "INVALID_REQUEST"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 사용자 조회 실패 예외
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("사용자 조회 실패: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                "USER_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 인터뷰 조회 실패 예외
     */
    @ExceptionHandler(InterviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInterviewNotFoundException(InterviewNotFoundException e) {
        log.warn("인터뷰 조회 실패: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                "INTERVIEW_NOT_FOUND"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 요청 데이터 검증 실패 (@Valid, @Validated)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("입력 데이터 검증 실패: {}", errorMessage);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "입력 데이터가 올바르지 않습니다: " + errorMessage,
                "VALIDATION_FAILED"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 요청 매개변수 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("매개변수 타입 불일치: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "요청 매개변수의 형식이 올바르지 않습니다",
                "TYPE_MISMATCH"
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 인증 실패 예외
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn("인증 실패: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                "인증이 필요합니다",
                "AUTHENTICATION_FAILED"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 권한 부족 예외
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("권한 부족: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "접근 권한이 없습니다",
                "ACCESS_DENIED"
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 정적 리소스 조회 실패 (조용히 처리)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();

        // favicon.ico 등은 로그 없이 처리
        if (resourcePath != null && (resourcePath.contains("favicon.ico") || resourcePath.contains(".ico"))) {
            return ResponseEntity.notFound().build();
        }

        log.debug("정적 리소스 조회 실패: {}", resourcePath);
        return ResponseEntity.notFound().build();
    }

    /**
     * 런타임 예외 (예상치 못한 오류)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("런타임 오류 발생: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "처리 중 오류가 발생했습니다",
                "RUNTIME_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 기타 모든 예외 (최종 방어선)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요",
                "INTERNAL_SERVER_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}