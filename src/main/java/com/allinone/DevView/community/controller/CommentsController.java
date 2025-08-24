package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.service.CommentsService;
import com.allinone.DevView.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Comments API", description = "커뮤니티 게시글 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommentsController {

    private final CommentsService commentsService;

    private Long getUserId(CustomUserDetails auth) { return (auth != null) ? auth.getUserId() : null; }
    private String getWriterName(CustomUserDetails auth) { return (auth != null) ? auth.getUsername() : "익명"; }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 페이지네이션하여 조회합니다.")
    @GetMapping(produces = "application/json")
    public Page<CommentsDto.Res> list(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 댓글 수 (1~50)") @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails auth
    ) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long me = getUserId(auth);
        return commentsService.list(postId, me, pageable);
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 새로운 댓글을 작성합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentsDto.Res create(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody CommentsDto.CreateReq req,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        String writerName = getWriterName(auth);
        return commentsService.create(postId, me, writerName, req);
    }

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글을 수정합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    @PutMapping(value = "/{commentId}", consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "수정할 댓글 ID") @PathVariable Long commentId,
            @Valid @RequestBody CommentsDto.UpdateReq req,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        try {
            commentsService.update(commentId, me, req);
        } catch (SecurityException se) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, se.getMessage());
        }
    }

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다. (로그인 필요)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "삭제할 댓글 ID") @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        try {
            commentsService.delete(commentId, me);
        } catch (SecurityException se) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, se.getMessage());
        }
    }
}
