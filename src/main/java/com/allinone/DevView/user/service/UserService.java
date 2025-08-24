package com.allinone.DevView.user.service;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.user.dto.request.LoginRequest;
import com.allinone.DevView.user.dto.request.PasswordChangeRequest;
import com.allinone.DevView.user.dto.request.RegisterRequest;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직 서비스
 * - LOCAL 사용자: 이메일/비밀번호 로그인
 * - GOOGLE 사용자: OAuth2 로그인 (provider_id 활용)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리 (LOCAL 사용자만)
     */
    public UserResponse register(RegisterRequest request) {
        log.info("회원가입 처리 시작: email={}", request.getEmail());

        validateRegisterRequest(request);

        User user = createLocalUser(request);
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: userId={}, email={}, provider={}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getProvider());
        return UserResponse.from(savedUser);
    }

    /**
     * 로그인 처리 (Spring Security가 아닌 직접 호출용)
     * LOCAL 사용자만 처리
     */
    public UserResponse login(LoginRequest request) {
        log.info("로그인 처리 시작: email={}", request.getEmail());

        User user = findLocalUserByEmail(request.getEmail());
        validateLoginCredentials(user, request.getPassword());

        log.info("로그인 성공: userId={}, provider={}", user.getUserId(), user.getProvider());
        return UserResponse.from(user);
    }

    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.debug("사용자 조회: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return UserResponse.from(user);
    }

    /**
     * 이메일로 LOCAL 사용자 조회 (Spring Security 핸들러용)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("이메일로 LOCAL 사용자 조회: email={}", email);

        User user = findLocalUserByEmail(email);
        return UserResponse.from(user);
    }

    /**
     * LOCAL provider 기준으로 이메일 사용 가능 여부 확인
     * 로컬 회원가입 시에는 LOCAL 사용자만 확인 (GOOGLE 사용자는 무시)
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        log.debug("LOCAL 이메일 중복 확인: email={}", email);

        // LOCAL 사용자 중에서만 이메일 중복 확인
        boolean available = !userRepository.existsByEmailAndProvider(email, "LOCAL");

        log.debug("LOCAL 이메일 사용 가능 여부: email={}, available={}", email, available);
        return available;
    }

    /**
     * 사용자명 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        log.debug("사용자명 중복 확인: username={}", username);

        boolean available = !userRepository.existsByUsername(username);

        log.debug("사용자명 사용 가능 여부: username={}, available={}", username, available);
        return available;
    }

    /**
     * 비밀번호 변경 처리
     * LOCAL 사용자만 가능 (GOOGLE 사용자는 불가)
     */
    @Transactional
    public UserResponse changePassword(Long userId, PasswordChangeRequest request) {
        log.info("비밀번호 변경 처리 시작: userId={}", userId);

        // 1. 입력값 검증
        validatePasswordChangeRequest(request);

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 3. OAuth2 사용자는 비밀번호 변경 불가
        if (user.isGoogleUser()) {
            log.warn("OAuth2 사용자가 비밀번호 변경 시도: userId={}, email={}, provider={}",
                    userId, user.getEmail(), user.getProvider());
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        // 4. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("현재 비밀번호 불일치: userId={}", userId);
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 5. 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);

        User savedUser = userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
        return UserResponse.from(savedUser);
    }

    /**
     * 회원탈퇴 처리 (하드 삭제)
     * 관련된 모든 데이터를 순서대로 삭제한 후 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("회원탈퇴 처리 시작: userId={}", userId);

        // 1. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        try {
            // 2. 관련 데이터 삭제 (User 포함, 모든 삭제를 Native Query로 처리)
            userRepository.deleteUserRelatedData(userId);

            log.info("회원탈퇴 완료: userId={}, email={}, provider={}",
                    userId, user.getEmail(), user.getProvider());

        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("회원탈퇴 처리 실패", e);
        }
    }

    // =====================================================================
    // Private Helper Methods
    // =====================================================================


    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.isPasswordMatched()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // LOCAL 사용자 중에서만 이메일 중복 확인
        if (userRepository.existsByEmailAndProvider(request.getEmail(), "LOCAL")) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다.");
        }
    }

    /**
     * LOCAL 사용자 생성
     */
    private User createLocalUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        return User.createLocalUser(
                request.getUsername(),
                request.getEmail(),
                encodedPassword
        );
    }

    /**
     * 이메일로 LOCAL 사용자만 찾기
     */
    private User findLocalUserByEmail(String email) {
        return userRepository.findLocalUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("LOCAL 사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 로그인 자격 증명 검증 (LOCAL 사용자용)
     */
    private void validateLoginCredentials(User user, String rawPassword) {
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("소셜 로그인을 이용해주세요.");
        }

        if (!user.isLocalUser()) {
            throw new IllegalArgumentException("소셜 로그인 사용자입니다. 해당 방식으로 로그인해주세요.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * 비밀번호 변경 요청 검증
     */
    private void validatePasswordChangeRequest(PasswordChangeRequest request) {
        // 1. 새 비밀번호 일치 확인
        if (!request.isNewPasswordMatched()) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        // 2. 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (request.isSameAsCurrentPassword()) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
    }
}