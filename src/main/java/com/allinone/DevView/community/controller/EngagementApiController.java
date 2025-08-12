package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.ToggleResponse;
import com.allinone.DevView.community.service.EngagementService;
import com.allinone.DevView.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class EngagementApiController {

    private final EngagementService engagementService;

    private Long resolveUserId(HttpSession session) {
        Object idObj = session.getAttribute("userId");
        if (idObj instanceof Long l) {
            return l;
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof UserResponse u) {
            Long uid = u.getUserId();
            if (uid != null) {
                session.setAttribute("userId", uid);
                return uid;
            }
        }

        return null;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ToggleResponse> toggleLike(@PathVariable Long postId,
                                                     HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(engagementService.toggleLike(postId, userId));
    }

    @PostMapping("/{postId}/scrap")
    public ResponseEntity<ToggleResponse> toggleScrap(@PathVariable Long postId,
                                                      HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(engagementService.toggleScrap(postId, userId));
    }
}
