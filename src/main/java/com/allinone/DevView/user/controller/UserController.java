package com.allinone.DevView.user.controller;

import com.allinone.DevView.user.dto.request.RegisterRequest;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 REST API 컨트롤러
 * GlobalExceptionHandler에서 예외 처리를 담당하므로 비즈니스 로직에만 집중
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     *
     * @param request 회원가입 요청 데이터
     * @return 생성된 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 요청: email={}", request.getEmail());

        UserResponse userResponse = userService.register(request);

        log.info("회원가입 완료: userId={}", userResponse.getUserId());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 사용자 정보 조회 API
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        log.debug("사용자 정보 조회: userId={}", userId);

        UserResponse userResponse = userService.getUserById(userId);

        return ResponseEntity.ok(userResponse);
    }

    /**
     * 이메일 중복 확인 API
     *
     * @param email 확인할 이메일
     * @return true=사용가능, false=사용불가
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        log.debug("이메일 중복 확인: email={}", email);

        boolean available = userService.isEmailAvailable(email);

        return ResponseEntity.ok(available);
    }

    /**
     * 사용자명 중복 확인 API
     *
     * @param username 확인할 사용자명
     * @return true=사용가능, false=사용불가
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        log.debug("사용자명 중복 확인: username={}", username);

        boolean available = userService.isUsernameAvailable(username);

        return ResponseEntity.ok(available);
    }
}