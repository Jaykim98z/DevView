package com.allinone.DevView.user.service;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.user.dto.request.LoginRequest;
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
 * 명확한 예외 처리와 일관된 응답을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     */
    public UserResponse register(RegisterRequest request) {
        log.info("회원가입 처리 시작: email={}", request.getEmail());

        validateRegisterRequest(request);

        User user = createLocalUser(request);
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    /**
     * 로그인 처리 (Spring Security가 아닌 직접 호출용)
     */
    public UserResponse login(LoginRequest request) {
        log.info("로그인 처리 시작: email={}", request.getEmail());

        User user = findUserByEmail(request.getEmail());
        validateLoginCredentials(user, request.getPassword());

        log.info("로그인 성공: userId={}", user.getUserId());
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
     * 이메일로 사용자 조회 (Spring Security 핸들러용)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("이메일로 사용자 조회: email={}", email);

        User user = findUserByEmail(email);
        return UserResponse.from(user);
    }

    /**
     * 이메일 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 사용자명 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    // === Private Helper Methods ===

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.isPasswordMatched()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다.");
        }
    }

    private User createLocalUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        return User.createLocalUser(
                request.getUsername(),
                request.getEmail(),
                encodedPassword
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

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
}