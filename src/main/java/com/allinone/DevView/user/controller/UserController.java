package com.allinone.DevView.user.controller;

import com.allinone.DevView.user.dto.request.LoginRequest;
import com.allinone.DevView.user.dto.request.RegisterRequest;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 컨트롤러
 * 회원가입, 로그인, 사용자 정보 조회 등의 HTTP 엔드포인트 제공
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
     * @param request 회원가입 요청 데이터 (username, email, password, passwordConfirm)
     * @return ResponseEntity<UserResponse> 생성된 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 API 호출: email={}", request.getEmail());

        try {
            UserResponse userResponse = userService.register(request);
            log.info("회원가입 API 성공: userId={}", userResponse.getUserId());
            return ResponseEntity.ok(userResponse);

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 API 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("회원가입 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 로그인 API
     *
     * @param request 로그인 요청 데이터 (email, password)
     * @return ResponseEntity<UserResponse> 로그인한 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 API 호출: email={}", request.getEmail());

        try {
            UserResponse userResponse = userService.login(request);
            log.info("로그인 API 성공: userId={}", userResponse.getUserId());
            return ResponseEntity.ok(userResponse);

        } catch (IllegalArgumentException e) {
            log.warn("로그인 API 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("로그인 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 정보 조회 API
     *
     * @param userId 조회할 사용자 ID
     * @return ResponseEntity<UserResponse> 사용자 정보
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        log.info("사용자 정보 조회 API 호출: userId={}", userId);

        try {
            UserResponse userResponse = userService.getUserById(userId);
            log.info("사용자 정보 조회 API 성공: userId={}", userId);
            return ResponseEntity.ok(userResponse);

        } catch (IllegalArgumentException e) {
            log.warn("사용자 정보 조회 API 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("사용자 정보 조회 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 이메일 중복 확인 API
     *
     * @param email 확인할 이메일
     * @return ResponseEntity<Boolean> true=사용가능, false=사용불가
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        log.info("이메일 중복 확인 API 호출: email={}", email);

        try {
            boolean available = userService.isEmailAvailable(email);
            log.info("이메일 중복 확인 API 성공: email={}, available={}", email, available);
            return ResponseEntity.ok(available);

        } catch (Exception e) {
            log.error("이메일 중복 확인 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자명 중복 확인 API
     *
     * @param username 확인할 사용자명
     * @return ResponseEntity<Boolean> true=사용가능, false=사용불가
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        log.info("사용자명 중복 확인 API 호출: username={}", username);

        try {
            boolean available = userService.isUsernameAvailable(username);
            log.info("사용자명 중복 확인 API 성공: username={}, available={}", username, available);
            return ResponseEntity.ok(available);

        } catch (Exception e) {
            log.error("사용자명 중복 확인 API 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}