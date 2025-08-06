package com.devview.user.service;

import com.devview.common.exception.CustomException;
import com.devview.common.exception.ErrorCode;
import com.devview.user.dto.request.LoginRequest;
import com.devview.user.entity.User;
import com.devview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**     * 로그인 인증     */
    public User authenticate(LoginRequest request) {
        return userRepository.findByEmailAndPassword(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    /**     * 사용자 ID로 조회     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**     * 사용자 프로필 수정     */
    @Transactional
    public void updateUserProfile(Long userId, UpdateRequest request) {
        User user = getUserById(userId);
        user.setUsername(request.getName());
        user.setJob(request.getJob());
        user.setCareerLevel(request.getCareerLevel());
        userRepository.save(user);
    }
}
