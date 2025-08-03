package com.devview.user.service;

import com.devview.user.dto.request.LoginRequest;
import com.devview.user.entity.User;
import com.devview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User authenticate(LoginRequest request) {
        return userRepository.findByEmailAndPassword(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));
    }
}
