package com.allinone.DevView.user.controller;

import com.allinone.DevView.user.dto.request.RegisterRequest;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.allinone.DevView.user.dto.request.PasswordChangeRequest;
import com.allinone.DevView.common.util.SecurityUtils;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 사용자 관련 REST API 컨트롤러
 * GlobalExceptionHandler에서 예외 처리를 담당하므로 비즈니스 로직에만 집중
 */
@Tag(name = "User API", description = "회원가입, 정보 조회, 중복 확인 등 사용자 관련 API")
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
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 사용자명 중복")
    })
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
    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 특정 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "조회할 사용자의 ID") @PathVariable Long userId) {
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
    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 사용할 이메일의 중복 여부를 확인합니다.")
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(
            @Parameter(description = "중복 확인할 이메일 주소") @RequestParam String email) {
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
    @Operation(summary = "사용자명 중복 확인", description = "회원가입 시 사용할 사용자명의 중복 여부를 확인합니다.")
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(
            @Parameter(description = "중복 확인할 사용자명") @RequestParam String username) {
        log.debug("사용자명 중복 확인: username={}", username);

        boolean available = userService.isUsernameAvailable(username);

        return ResponseEntity.ok(available);
    }

    /**
     * 비밀번호 변경 API
     *
     * @param request 비밀번호 변경 요청 데이터
     * @return 변경된 사용자 정보
     */
    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 현재 비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/password")
    public ResponseEntity<UserResponse> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        Long userId = SecurityUtils.getUserId();
        log.info("비밀번호 변경 요청: userId={}", userId);

        UserResponse userResponse = userService.changePassword(userId, request);

        log.info("비밀번호 변경 완료: userId={}", userId);
        return ResponseEntity.ok(userResponse);
    }

}