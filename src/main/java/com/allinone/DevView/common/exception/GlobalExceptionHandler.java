package com.allinone.DevView.common.exception;

import com.allinone.DevView.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * 모든 컨트롤러에서 발생하는 예외를 통합 처리하여 일관된 에러 응답 제공
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리 (IllegalArgumentException)
     * 사용자 입력 오류, 중복 데이터, 비즈니스 규칙 위반 등
     *
     * @param e IllegalArgumentException
     * @return ResponseEntity<ErrorResponse> 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("비즈니스 로직 예외 발생: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getMessage(), "INVALID_INPUT");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 요청 데이터 검증 실패 처리 (@Valid 어노테이션)
     * @NotBlank, @Email, @Size 등의 검증 실패
     *
     * @param e MethodArgumentNotValidException
     * @return ResponseEntity<ErrorResponse> 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("입력값 검증 실패: {}", e.getMessage());

        // 첫 번째 검증 실패 메시지 사용
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String message = fieldError.getDefaultMessage();

        ErrorResponse errorResponse = ErrorResponse.of(message, "VALIDATION_FAILED");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 사용자를 찾을 수 없음 예외 처리
     *
     * @param e UserNotFoundException
     * @return ResponseEntity<ErrorResponse> 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("사용자 조회 실패: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getMessage(), "USER_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 예상치 못한 시스템 오류 처리
     * 모든 처리되지 않은 예외의 최종 처리
     *
     * @param e Exception
     * @return ResponseEntity<ErrorResponse> 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류 발생", e);

        ErrorResponse errorResponse = ErrorResponse.of(
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}