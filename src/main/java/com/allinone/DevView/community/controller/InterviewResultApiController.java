package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.service.InterviewResultQueryService;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Interview Result API", description = "공유된 면접 결과 조회 관련 API")
@RestController
@RequestMapping("/api/community/interview-results")
@RequiredArgsConstructor
public class InterviewResultApiController {

    private final InterviewResultQueryService service;

    @Operation(summary = "최신 면접 결과 조회", description = "현재 로그인된 사용자의 가장 최근 면접 결과를 조회합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "면접 결과 없음")
    })
    @GetMapping("/latest")
    public ResponseEntity<InterviewResultResponse> latest(HttpSession session) {
        Long userId = resolveUserId(session);
        return ResponseEntity.ok(service.findLatestByUserId(userId));
    }


    @Operation(summary = "특정 면접 결과 조회", description = "ID로 특정 면접 결과를 조회합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접 결과")
    })
    @GetMapping("/{resultId}")
    public ResponseEntity<InterviewResultResponse> byId(
            @Parameter(description = "조회할 면접 결과의 ID") @PathVariable Long resultId, HttpSession session) {
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
