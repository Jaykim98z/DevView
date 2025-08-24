package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.ToggleResponse;
import com.allinone.DevView.community.service.EngagementService;
import com.allinone.DevView.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Engagement API", description = "게시글 좋아요 및 스크랩 관련 API")
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

    @Operation(summary = "게시글 좋아요 토글", description = "사용자가 특정 게시글에 '좋아요'를 누르거나 취소합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공. 'toggledOn' 필드로 현재 상태 확인"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<ToggleResponse> toggleLike(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
                                                     HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(engagementService.toggleLike(postId, userId));
    }

    @Operation(summary = "게시글 스크랩 토글", description = "사용자가 특정 게시글을 스크랩하거나 취소합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공. 'toggledOn' 필드로 현재 상태 확인"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<ToggleResponse> toggleScrap(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
                                                      HttpSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(engagementService.toggleScrap(postId, userId));
    }
}
