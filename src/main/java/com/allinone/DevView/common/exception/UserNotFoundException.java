package com.allinone.DevView.common.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 커스텀 예외
 * 404 Not Found 응답을 위한 예외 클래스
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 메시지와 함께 예외 생성
     *
     * @param message 예외 메시지
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * 사용자 ID를 포함한 예외 생성
     *
     * @param userId 찾을 수 없는 사용자 ID
     */
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. (ID: " + userId + ")");
    }
}