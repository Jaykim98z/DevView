package com.allinone.DevView.user.service;

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
 * 사용자 관련 비즈니스 로직
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
     *
     * @param request 회원가입 요청 데이터
     * @return UserResponse 생성된 사용자 정보
     * @throws IllegalArgumentException 검증 실패 시
     */
    public UserResponse register(RegisterRequest request) {
        log.info("회원가입 시도: email={}, username={}", request.getEmail(), request.getUsername());

        // 1. 비밀번호 일치 확인
        if (!request.isPasswordMatched()) {
            log.warn("회원가입 실패 - 비밀번호 불일치: email={}", request.getEmail());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("회원가입 실패 - 이메일 중복: email={}", request.getEmail());
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 3. 사용자명 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("회원가입 실패 - 사용자명 중복: username={}", request.getUsername());
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다.");
        }

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료: email={}", request.getEmail());

        // 5. User 엔티티 생성
        User user = User.createLocalUser(
                request.getUsername(),
                request.getEmail(),
                encodedPassword
        );

        // 6. 데이터베이스에 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}, username={}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getUsername());

        // 7. UserResponse로 변환해서 반환
        return UserResponse.from(savedUser);
    }

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 데이터
     * @return UserResponse 로그인한 사용자 정보
     * @throws IllegalArgumentException 로그인 실패 시
     */
    public UserResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", request.getEmail());

        // 1. 이메일로 사용자 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자 없음: email={}", request.getEmail());
                    return new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        // 2. 로컬 사용자인지 확인 (구글 사용자 제외)
        if (!user.isLocalUser()) {
            log.warn("로그인 실패 - 소셜 사용자: email={}, provider={}", request.getEmail(), user.getProvider());
            throw new IllegalArgumentException("소셜 로그인 사용자입니다. 해당 방식으로 로그인해주세요.");
        }

        // 3. 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("로그인 성공: userId={}, email={}", user.getUserId(), user.getEmail());
        return UserResponse.from(user);
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param userId 조회할 사용자 ID
     * @return UserResponse 사용자 정보
     * @throws IllegalArgumentException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.debug("사용자 정보 조회: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자 조회 실패 - 존재하지 않음: userId={}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        log.debug("사용자 정보 조회 성공: userId={}, email={}", user.getUserId(), user.getEmail());
        return UserResponse.from(user);
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 확인할 이메일
     * @return boolean true=사용가능, false=사용불가
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        log.debug("이메일 중복 확인: email={}", email);

        boolean available = !userRepository.existsByEmail(email);

        log.debug("이메일 중복 확인 결과: email={}, available={}", email, available);
        return available;
    }

    /**
     * 사용자명 중복 확인
     *
     * @param username 확인할 사용자명
     * @return boolean true=사용가능, false=사용불가
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        log.debug("사용자명 중복 확인: username={}", username);

        boolean available = !userRepository.existsByUsername(username);

        log.debug("사용자명 중복 확인 결과: username={}, available={}", username, available);
        return available;
    }

}