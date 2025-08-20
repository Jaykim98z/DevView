package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.service.InterviewResultQueryService;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/community/interview-results")
@RequiredArgsConstructor
public class InterviewResultApiController {

    private final InterviewResultQueryService service;

    @GetMapping("/latest")
    public ResponseEntity<InterviewResultResponse> latest(HttpSession session) {
        Long userId = resolveUserId(session);
        return ResponseEntity.ok(service.findLatestByUserId(userId));
    }


    @GetMapping("/{resultId}")
    public ResponseEntity<InterviewResultResponse> byId(@PathVariable Long resultId, HttpSession session) {
        Long userId = resolveUserId(session);
        return ResponseEntity.ok(service.findByIdForUser(resultId, userId));
    }


    private Long resolveUserId(HttpSession session) {
        Object idObj = session.getAttribute("userId");
        if (idObj instanceof Long l) return l;

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserResponse u && u.getUserId() != null) {
            Long uid = u.getUserId();
            session.setAttribute("userId", uid);
            return uid;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
}
